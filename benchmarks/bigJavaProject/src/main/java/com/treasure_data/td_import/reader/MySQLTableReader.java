//
// Treasure Data Bulk-Import Tool in Java
//
// Copyright (C) 2012 - 2013 Muga Nishizawa
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
package com.treasure_data.td_import.reader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.treasure_data.td_import.Configuration;
import com.treasure_data.td_import.model.AliasTimeColumnValue;
import com.treasure_data.td_import.model.ColumnType;
import com.treasure_data.td_import.model.TimeColumnValue;
import com.treasure_data.td_import.prepare.MySQLPrepareConfiguration;
import com.treasure_data.td_import.prepare.PreparePartsException;
import com.treasure_data.td_import.prepare.Task;
import com.treasure_data.td_import.writer.RecordWriter;
import com.treasure_data.td_import.writer.JSONRecordWriter;
import com.treasure_data.td_import.writer.MySQLTimestampAdaptedJSONRecordWriter;

public class MySQLTableReader extends AbstractRecordReader<MySQLPrepareConfiguration> {
    private static final Logger LOG = Logger.getLogger(MySQLTableReader.class.getName());

    private static final String QUERY_SAMPLE = "SELECT * FROM %s LIMIT 1;";
    private static final String QUERY = "SELECT * FROM %s;";

    protected Connection conn;
    protected List<Object> row = new ArrayList<Object>();
    protected int numColumns;
    protected ResultSet resultSet;

    public MySQLTableReader(MySQLPrepareConfiguration conf, RecordWriter writer) {
        super(conf, writer);
    }

    @Override
    public void configure(Task task) throws PreparePartsException {
        super.configure(task);

        try {
            Class.forName(Configuration.BI_PREPARE_PARTS_MYSQL_JDBCDRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new PreparePartsException(e);
        } 

        String url = conf.getJdbcUrl();
        String user = conf.getUser();
        String password = conf.getPassword();
        String table = task.getSource().getPath();

        // create and test a connection
        try {
            conn = DriverManager.getConnection(url, user, password);
            String msg = String.format("Connected successfully to %s", url);
            System.out.println(msg);
            LOG.info(msg);
        } catch (SQLException e) {
            throw new PreparePartsException(e);
        }

        // sample
        sample(table);

        Statement stat = null;
        try {
            stat = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
                    java.sql.ResultSet.CONCUR_READ_ONLY);
            stat.setFetchSize(Integer.MIN_VALUE);
            // TODO optimize the query string
            resultSet = stat.executeQuery(String.format(QUERY, table));
        } catch (SQLException e) {
            throw new PreparePartsException(e);
        }
    }

    private void setColumnNames(ResultSetMetaData metadata) throws SQLException {
        int count = metadata.getColumnCount();

        if (columnNames != null && columnNames.length != 0) {
            if (count != columnNames.length) {
                throw new IllegalArgumentException(String.format(
                        "The number of specified columns (%d) must " +
                        "match the number of columns (%d) in the table",
                        columnNames.length, count));
            }
        }

        if (columnNames == null || columnNames.length == 0) {
            String[] cnames = new String[numColumns];
            for (int i = 0; i < numColumns; i++) {
                cnames[i] = metadata.getColumnName(i + 1);
            }
            conf.setColumnNames(cnames);
            columnNames = conf.getColumnNames();
            actualColumnNames = conf.getActualColumnNames();
        }
    }

    private void  setTimeColumnValue(int timeColumnIndex, int aliasTimeColumnIndex) {
        int index = -1;
        boolean isAlias = false;

        if (timeColumnIndex >= 0) {
            index = timeColumnIndex;
            isAlias = false;
        } else if (aliasTimeColumnIndex >= 0) {
            index = aliasTimeColumnIndex;
            isAlias = true;
        }

        if (index < 0) {
            timeColumnValue = conf.getTimeValue();
        } else {
            if (!isAlias) {
                timeColumnValue = new TimeColumnValue(index, conf.getTimeFormat());
            } else {
                timeColumnValue = new AliasTimeColumnValue(index, conf.getTimeFormat());
            }
        }
    }

    private void sample(String table) throws PreparePartsException {
        Statement sampleStat = null;
        ResultSet sampleResultSet = null;

        try {
            // TODO FIXME
            // here, we should use conn.getMetaData().getColumns(..).
            sampleStat = conn.createStatement();
            sampleResultSet = sampleStat.executeQuery(String.format(QUERY_SAMPLE, table));
            ResultSetMetaData metaData = sampleResultSet.getMetaData();
            numColumns = metaData.getColumnCount();

            this.setColumnNames(metaData);

            // get index of 'time' column
            // [ "time", "name", "price" ] as all columns is given,
            // the index is zero.
            int timeColumnIndex = getTimeColumnIndex();

            // get index of specified alias time column
            // [ "timestamp", "name", "price" ] as all columns and
            // "timestamp" as alias time column are given, the index is zero.
            //
            // if 'time' column exists in row data, the specified alias
            // time column is ignore.
            int aliasTimeColumnIndex = getAliasTimeColumnIndex(timeColumnIndex);

            // if 'time' and the alias columns don't exist, ...
            if (timeColumnIndex < 0 && aliasTimeColumnIndex < 0) {
                if (conf.getTimeValue().getTimeValue() >= 0) {
                } else {
                    throw new PreparePartsException(
                            "Time column not found. --time-column or --time-value option is required");
                }
            }

            List<Object> firstRow = new ArrayList<Object>();
            if (sampleResultSet.next()) {
                for (int i = 0; i < numColumns; i++) {
                    firstRow.add(sampleResultSet.getObject(i + 1));
                }
            }

            // initialize types of all columns
            if (columnTypes == null || columnTypes.length == 0) {
                // 'all-string' option is ignored
                columnTypes = new ColumnType[numColumns];
                for (int i = 0; i < numColumns; i++) {
                    columnTypes[i] = toColumnType(
                            metaData.getColumnType(i + 1),
                            metaData.getColumnTypeName(i + 1),
                            metaData.isSigned(i + 1));
                }
            }

            // initialize time column value
            setTimeColumnValue(timeColumnIndex, aliasTimeColumnIndex);

            initializeWrittenRecord();

            // check properties of exclude/only columns
            setSkipColumns();

            JSONRecordWriter w = null;
            try {
                w = new MySQLTimestampAdaptedJSONRecordWriter(conf);
                w.setActualColumnNames(getActualColumnNames());
                w.setColumnNames(getColumnNames());
                w.setColumnTypes(getColumnTypes());
                w.setSkipColumns(getSkipColumns());
                w.setTimeColumnValue(getTimeColumnValue());

                this.row.addAll(firstRow);

                // convert each column in row
                convertTypes();
                // write each column value
                w.next(writtenRecord);
                String ret = w.toJSONString();
                String msg = null;
                if (ret != null) {
                    msg = "sample row: " + ret;
                } else  {
                    msg = "cannot get sample row";
                }
                System.out.println(msg);
                LOG.info(msg);
            } finally {
                if (w != null) {
                    w.close();
                }
            }
        } catch (IOException e) {
            throw new PreparePartsException(e);
        } catch (SQLException e) {
            throw new PreparePartsException(e);
        } finally {
            if (sampleResultSet != null) {
                try {
                    sampleResultSet.close();
                } catch (SQLException e) {
                    throw new PreparePartsException(e);
                }
            }

            if (sampleStat != null) {
                try {
                    sampleStat.close();
                } catch (SQLException e) {
                    throw new PreparePartsException(e);
                }
            }
        }
    }

    //@VisibleForTesting
    static ColumnType toColumnType(int jdbcType, String typeName, boolean signed)
            throws PreparePartsException {
        switch (jdbcType) {
        case Types.BIT:
            return ColumnType.BOOLEAN;

        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            return ColumnType.STRING;

        case Types.TINYINT:
            // 'TINYINT'
            // 'TINYINT UNSIGNED'
        case Types.SMALLINT:
            // 'SMALLINT'
            // 'SMALLINT UNSIGNED'
            return ColumnType.INT;

        case Types.INTEGER: // INT
            if (typeName.startsWith("MEDIUMINT")) {
                // 'MEDIUMINT'
                // 'MEDIUMINT UNSIGNED'
                return ColumnType.INT;
            }
            return !signed ? ColumnType.LONG : ColumnType.INT;

        case Types.BIGINT:
            if (signed) {
                // 'BIGINT'
                return ColumnType.LONG;
            } else {
                // 'BIGINT UNSIGNED'
                throw new PreparePartsException("UNSIGNED BIGINT is not supported.");
            }

        case Types.FLOAT:
        case Types.DOUBLE:
            return ColumnType.DOUBLE;
        case Types.TIME:
        case Types.TIMESTAMP: // DATETIME, TIMESTAMP
            return new MySQLPrepareConfiguration.TimestampColumnType();
        case Types.DATE: // DATE
            return new MySQLPrepareConfiguration.DateColumnType();
        default:
            throw new PreparePartsException("unsupported jdbc type: " + jdbcType);
        }
    }

    @Override
    public boolean readRecord() throws IOException {
        row.clear();
        try {
            boolean hasNext = resultSet.next();
            if (!hasNext) {
                return false;
            }

            for (int i = 0; i < numColumns; i++) {
                row.add(i, resultSet.getObject(i + 1));
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }

        return true;
    }

    @Override
    public void convertTypes() throws PreparePartsException {
        for (int i = 0; i < row.size(); i++) {
            columnTypes[i].setColumnValue(row.get(i), writtenRecord.getValue(i));
        }
    }

    @Override
    public String getCurrentRecord() {
        return row.toString();
    }

    @Override
    public void close() throws IOException {
        super.close();

        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                throw new IOException(e);
            }
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new IOException(e);
            }
        }
    }
}
