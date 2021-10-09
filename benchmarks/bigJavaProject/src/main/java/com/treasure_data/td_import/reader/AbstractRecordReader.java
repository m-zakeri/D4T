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

import com.treasure_data.td_import.Configuration;
import com.treasure_data.td_import.model.*;
import com.treasure_data.td_import.prepare.*;
import com.treasure_data.td_import.source.Source;
import com.treasure_data.td_import.writer.RecordWriter;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractRecordReader<T extends PrepareConfiguration>
        implements RecordReader<T> {
    private static final Logger LOG = Logger.getLogger(AbstractRecordReader.class.getName());

    protected T conf;
    protected RecordWriter writer;
    protected Record writtenRecord;

    protected Source source;
    protected String[] actualColumnNames;
    protected String[] columnNames;
    protected ColumnType[] columnTypes;
    protected Set<String> skipColumns = new HashSet<String>();

    protected String primaryKey;
    protected ColumnType primaryKeyType;
    protected TimeColumnValue timeColumnValue;

    protected long lineNum = 0;

    // fields for writing invalid records
    private Writer errWriter;
    private boolean errWriterCreated = false;

    protected AbstractRecordReader(T conf, RecordWriter writer) {
        this.conf = conf;
        this.writer = writer;
    }

    public void configure(Task task) throws PreparePartsException {
        source = task.getSource();
        actualColumnNames = conf.getActualColumnNames();
        columnNames = conf.getColumnNames();
        columnTypes = conf.getColumnTypes();

        // check compression type of the file
        conf.checkCompressionType(source);
    }

    public void resetLineNum() {
        lineNum = 0;
    }

    public void incrementLineNum() {
        lineNum++;
    }

    public long getLineNum() {
        return lineNum;
    }

    public String[] getActualColumnNames() {
        return actualColumnNames;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public ColumnType[] getColumnTypes() {
        return columnTypes;
    }

    public Set<String> getSkipColumns() {
        return skipColumns;
    }

    public void setSkipColumns() {
        String[] excludeColumns = conf.getExcludeColumns();
        String[] onlyColumns = conf.getOnlyColumns();
        for (int i = 0; i < actualColumnNames.length; i++) {
            // check exclude columns
            boolean isExcluded = false;
            for (String excludeColumn : excludeColumns) {
                if (actualColumnNames[i].equals(excludeColumn)) {
                    isExcluded = true;
                    break;
                }
            }

            if (isExcluded) {
                skipColumns.add(actualColumnNames[i]);
                continue;
            }

            // check only columns
            if (onlyColumns.length == 0) {
                continue;
            }

            boolean isOnly = false;
            for (String onlyColumn : onlyColumns) {
                if (actualColumnNames[i].equals(onlyColumn)) {
                    isOnly = true;
                    break;
                }
            }

            if (!isOnly) {
                skipColumns.add(actualColumnNames[i]);
                continue; // not needed though,..
            }
        }
    }

    public int getTimeColumnIndex() {
        int index = -1;

        if (conf.hasPrimaryKey()) {
            return index;
        }

        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i].equals(
                    Configuration.BI_PREPARE_PARTS_TIMECOLUMN_DEFAULTVALUE)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public int getAliasTimeColumnIndex(int timeColumnIndex) {
        int index = -1;

        if (conf.hasPrimaryKey()) {
            return index;
        }

        if (timeColumnIndex < 0 && conf.getAliasTimeColumn() != null) {
            for (int i = 0; i < columnNames.length; i++) {
                if (columnNames[i].equals(conf.getAliasTimeColumn())) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    public void validateTimeAndPrimaryColumn(int timeColumnIndex, int aliasTimeColumnIndex)
            throws PreparePartsException {
        // check primary key
        if (conf.hasPrimaryKey()) {
            boolean has = false;
            for (String cname : columnNames) {
                if (cname.equals(conf.getPrimaryKey())) {
                    has = true;
                    break;
                }
            }
            if (!has) {
                throw new PreparePartsException(
                        "Primary key column not found. Please check your data");
            }
            return;
        }

        // check time and the alias column
        if (timeColumnIndex < 0 && aliasTimeColumnIndex < 0) {
            if (conf.getTimeValue().getTimeValue() >= 0) {
            } else {
                throw new PreparePartsException(
                        "Time column not found. --time-column or --time-value option is required");
            }
        }
    }

    public void initializeColumnTypes(TimeColumnSampling[] sampleColumnValues) {
        if (columnTypes == null || columnTypes.length == 0) {
            columnTypes = new ColumnType[columnNames.length];
            for (int i = 0; i < columnTypes.length; i++) {
                if (conf.hasPrimaryKey() && conf.getPrimaryKey().equals(columnNames[i])) {
                    columnTypes[i] = conf.getPrimaryKeyType();
                    continue;
                }

                if (conf.getColumnTypeMap().containsKey(columnNames[i])) {
                    columnTypes[i] = conf.getColumnTypeMap().get(columnNames[i]);
                    continue;
                }

                if (conf.hasAllString()) {
                    columnTypes[i] = ColumnType.STRING;
                    continue;
                }

                columnTypes[i] = sampleColumnValues[i].getColumnTypeRank();
            }
            conf.setColumnTypes(columnTypes);
        }
    }

    public TimeColumnValue getTimeColumnValue() {
        return timeColumnValue;
    }

    public void setTimeColumnValue(TimeColumnSampling[] sampleColumnValues,
            int timeColumnIndex, int aliasTimeColumnIndex) {
        int index = -1;
        boolean isAlias = false;

        if (conf.hasPrimaryKey()) {
            timeColumnValue = new SkippedTimeColumnValue();
            return;
        } else if (timeColumnIndex >= 0) {
            // if users specify time-value option, the existing time colume values
            // in their files are overwritten to the value specified as time-value.
            if (conf.getTimeValue().getTimeValue() >= 0) {
                timeColumnValue = conf.getTimeValue();
                return;
            }
            index = timeColumnIndex;
            isAlias = false;
        } else if (aliasTimeColumnIndex >= 0) {
            index = aliasTimeColumnIndex;
            isAlias = true;
        }

        if (index < 0) {
            timeColumnValue = conf.getTimeValue();
        } else if (conf.getTimeFormat() != null) {
            timeColumnValue = createTimeColumnValue(index, isAlias, conf.getTimeFormat());
        } else {
            String suggested = sampleColumnValues[index].getSTRFTimeFormatRank();
            if (suggested != null) {
                if (suggested.equals(TimeColumnSampling.HHmmss_STRF)) {
                    timeColumnValue = createTimeColumnValue(index, isAlias, new HHmmssStrftime());
                } else {
                    timeColumnValue = createTimeColumnValue(index, isAlias, conf.getTimeFormat(suggested));
                }
            } else {
                timeColumnValue = createTimeColumnValue(index, isAlias, null);
            }
        }
    }

    private TimeColumnValue createTimeColumnValue(int index, boolean isAlias, Strftime strftime) {
        if (!isAlias) {
            return new TimeColumnValue(index, strftime);
        } else {
            return new AliasTimeColumnValue(index, strftime);
        }
    }

    public void initializeWrittenRecord() {
        ColumnValue[] values = new ColumnValue[columnTypes.length];
        for (int i = 0; i < columnTypes.length; i++) {
            values[i] = columnTypes[i].createColumnValue(conf, i);
        }
        writtenRecord = new Record(values);
    }

    public boolean next() throws PreparePartsException {
        try {
            // if reader got EOF, it returns false.
            if (!readRecord()) {
                return false;
            }

            // convert each column in row
            convertTypes();

            // write each column value
            writer.next(writtenRecord);

            writer.incrementRowNum();
        } catch (IOException e) {
            // if reader throw I/O error, parseRow throws PreparePartsException.
            String msg = String.format("Cannot read raw data: line %d in %s", lineNum, source);
            LOG.log(Level.SEVERE, msg, e);
            throw new PreparePartsException(e);
        } catch (PreparePartsException e) {
            writer.incrementErrorRowNum();

            // the untokenized raw row is written to error rows file
            writeErrorRecord(getCurrentRecord());
            // the untokenized raw row is written to LOG
            String msg = String.format("line %d in %s: %s", lineNum, source, getCurrentRecord());
            LOG.log(Level.WARNING, msg, e);

            handleError(e);
        }
        return true;
    }

    public abstract boolean readRecord() throws IOException, PreparePartsException;

    public abstract void convertTypes() throws PreparePartsException;

    public abstract String getCurrentRecord();

    public void handleError(PreparePartsException e) throws PreparePartsException {
        conf.getErrorRecordsHandling().handleError(e);
    }

    // Closeable#close()
    public void close() throws IOException {
        // file writer must not be closed. because PrepareProcessor will do that.

        closeErrWriter();
    }

    public void writeErrorRecord(String record) {
        if (!errWriterCreated) {
            createErrWriter();
        }

        try {
            errWriter.write(String.format("(%d) %s\n", lineNum, record));
        } catch (IOException e) {
            LOG.log(Level.WARNING, String.format(
                    "error records cannot be written: %s", record), e);
        }
    }

    public void createErrWriter() {
        String errDirName = conf.getErrorRecordsOutputDirName();
        String srcName = source.getPath();
        int lastSepIndex = srcName.lastIndexOf(source.getSeparatorChar());
        String prefix = srcName.substring(lastSepIndex + 1, srcName.length()).replace('.', '_');
        String errFileName = prefix + ".error-records.txt";

        File dir = new File(errDirName);
        if (dir.exists()) {
            dir.mkdirs();
        }

        File errFile = new File(errDirName, errFileName);
        if (errFile.exists()) {
            errFile.delete();
        }

        try {
            errWriterCreated = errFile.createNewFile();
            LOG.info(String.format("error records file is created: %s", errFile.getAbsolutePath()));
        } catch (IOException e) {
            LOG.log(Level.WARNING, "error records file cannot be created", e);
        }

        try {
            errWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(errFile)));
        } catch (FileNotFoundException e) {
            LOG.log(Level.WARNING, "error records writer cannot be created", e);
        }
    }

    public void closeErrWriter() {
        if (errWriterCreated) {
            if (errWriter != null) {
                try {
                    errWriter.flush();
                } catch (IOException e) {
                    // ignore
                }

                try {
                    errWriter.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

}
