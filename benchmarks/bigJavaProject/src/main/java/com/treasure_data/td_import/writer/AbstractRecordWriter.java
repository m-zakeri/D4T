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
package com.treasure_data.td_import.writer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.treasure_data.td_import.Configuration;
import com.treasure_data.td_import.model.AliasTimeColumnValue;
import com.treasure_data.td_import.model.ColumnType;
import com.treasure_data.td_import.model.DoubleColumnValue;
import com.treasure_data.td_import.model.IntColumnValue;
import com.treasure_data.td_import.model.LongColumnValue;
import com.treasure_data.td_import.model.Record;
import com.treasure_data.td_import.model.SkippedTimeColumnValue;
import com.treasure_data.td_import.model.StringColumnValue;
import com.treasure_data.td_import.model.TimeColumnValue;
import com.treasure_data.td_import.model.TimeValueTimeColumnValue;
import com.treasure_data.td_import.prepare.PrepareConfiguration;
import com.treasure_data.td_import.prepare.PreparePartsException;
import com.treasure_data.td_import.prepare.Task;
import com.treasure_data.td_import.prepare.TaskResult;

public abstract class AbstractRecordWriter implements RecordWriter {
    private static final Logger LOG = Logger.getLogger(AbstractRecordWriter.class.getName());

    protected PrepareConfiguration conf;
    protected Task task;
    protected TaskResult result;
    protected long rowNum = 0;
    protected long errorNum = 0;

    protected String[] actualColumnNames;
    protected String[] columnNames;
    protected ColumnType[] columnTypes;

    protected Set<String> skipColumns;

    protected boolean needAdditionalTimeColumn = false;
    protected TimeColumnValue timeColumnValue;
    protected int timeColumnIndex = -1;

    protected boolean hasPrimaryKey;

    protected AbstractRecordWriter(PrepareConfiguration conf) {
        this.conf = conf;
    }

    public void setActualColumnNames(String[] actualColumnNames) {
        this.actualColumnNames = actualColumnNames;
    }

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

    public void setColumnTypes(ColumnType[] columnTypes) {
        this.columnTypes = columnTypes;
    }

    public void setSkipColumns(Set<String> skipColumns) {
        this.skipColumns = skipColumns;
    }

    public void setTimeColumnValue(TimeColumnValue timeColumnValue) {
        needAdditionalTimeColumn =
                timeColumnValue instanceof AliasTimeColumnValue ||
                timeColumnValue instanceof TimeValueTimeColumnValue;
        if (!needAdditionalTimeColumn) {
            if (!(timeColumnValue instanceof SkippedTimeColumnValue)) {
                timeColumnIndex = ((TimeColumnValue) timeColumnValue).getIndex();
            } else {
                hasPrimaryKey = true;
            }
        }
        this.timeColumnValue = timeColumnValue;
    }

    public void configure(Task task, TaskResult result)
            throws PreparePartsException {
        this.task = task;
        this.result = result;
    }

    public void next(Record row) throws PreparePartsException {
        int size = row.getValues().length;
        int actualSize = 0;
        int offset = 0;

        // validate time column
        if (needAdditionalTimeColumn) {
            timeColumnValue.validate(row.getValue(timeColumnValue.getIndex()), this);
        } else {
            if (!hasPrimaryKey) {
                timeColumnValue.validate(row.getValue(timeColumnIndex), this);
            }
        }

        try {
            if (needAdditionalTimeColumn) {
                // if the row doesn't have 'time' column, new 'time' column
                // needs to be appended to it.
                actualSize = size - skipColumns.size() + 1;
            } else {
                actualSize = size - skipColumns.size();
            }

            // write columns as map data
            writeBeginRow(actualSize);
            for (int i = 0; i < size; i++) {
                if (skipColumns.contains(actualColumnNames[i])) {
                    continue;
                }

                write(columnNames[i]);
                offset++;

                if (!hasPrimaryKey && i == timeColumnIndex) {
                    timeColumnValue.write(row.getValue(i), this);
                } else {
                    row.getValue(i).write(this);
                }
                offset++;
            }

            if (needAdditionalTimeColumn) {
                write(Configuration.BI_PREPARE_PARTS_TIMECOLUMN_DEFAULTVALUE);
                timeColumnValue.write(row.getValue(timeColumnValue.getIndex()), this);
            }
        } catch (PreparePartsException e) {
            // fill nil values TODO FIXME we should roll back though,..
            int rest = actualSize * 2 - offset;
            for (int i = 0; i < rest; i++) {
                writeNil();
            }

            throw e;
        } finally {
            // end
            writeEndRow();
        }
    }

    public abstract void writeBeginRow(int size) throws PreparePartsException;
    public abstract void writeNil() throws PreparePartsException;
    public abstract void write(String v) throws PreparePartsException;
    public abstract void write(int v) throws PreparePartsException;
    public abstract void write(long v) throws PreparePartsException;
    public abstract void write(double v) throws PreparePartsException;
    public abstract void write(List<Object> v) throws PreparePartsException;
    public abstract void write(Map<Object, Object> v) throws PreparePartsException;

    public void write(TimeColumnValue filter, StringColumnValue v) throws PreparePartsException {
        String timeString = v.getString();
        long time = 0;

        if (filter.getTimeFormat() != null) {
            time = filter.getTimeFormat().getTime(timeString);
        }

        if (time == 0) {
            try {
                time = Long.parseLong(timeString);
            } catch (Throwable t) {
                ;
            }
        }

        write(time);
    }

    public void write(TimeColumnValue filter, IntColumnValue v) throws PreparePartsException {
        v.write(this);
    }

    public void write(TimeColumnValue filter, LongColumnValue v) throws PreparePartsException {
        v.write(this);
    }

    public void write(TimeColumnValue filter, DoubleColumnValue v) throws PreparePartsException {
        v.writeAsLong(this);
    }

    public void validate(TimeColumnValue filter, StringColumnValue v) throws PreparePartsException {
        String timeString = v.getString();
        long time = 0;

        if (filter.getTimeFormat() != null) {
            time = filter.getTimeFormat().getTime(timeString);
        }

        if (time == 0) {
            try {
                time = Long.parseLong(timeString);
            } catch (Throwable t) {
                ;
            }
        }

        filter.validateUnixtime(time);
    }

    public void validate(TimeColumnValue filter, IntColumnValue v) throws PreparePartsException {
        filter.validateUnixtime(v.getInt());
    }

    public void validate(TimeColumnValue filter, LongColumnValue v) throws PreparePartsException {
        filter.validateUnixtime(v.getLong());
    }

    public void validate(TimeColumnValue filter, DoubleColumnValue v) throws PreparePartsException {
        filter.validateUnixtime((long) v.getDouble());
    }

    public abstract void writeEndRow() throws PreparePartsException;

    public void resetRowNum() {
        rowNum = 0;
    }

    public void incrementRowNum() {
        rowNum++;
    }

    public long getRowNum() {
        return rowNum;
    }

    public void resetErrorRowNum() {
        errorNum = 0;
    }

    public void incrementErrorRowNum() {
        errorNum++;
    }

    public long getErrorRowNum() {
        return errorNum;
    }

    // Closeable#close()
    public abstract void close() throws IOException;
}
