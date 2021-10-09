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

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.treasure_data.td_import.model.ColumnType;
import com.treasure_data.td_import.model.DoubleColumnValue;
import com.treasure_data.td_import.model.IntColumnValue;
import com.treasure_data.td_import.model.LongColumnValue;
import com.treasure_data.td_import.model.Record;
import com.treasure_data.td_import.model.StringColumnValue;
import com.treasure_data.td_import.model.TimeColumnValue;
import com.treasure_data.td_import.prepare.PreparePartsException;
import com.treasure_data.td_import.prepare.Task;
import com.treasure_data.td_import.prepare.TaskResult;

public interface RecordWriter extends Closeable {

    void setActualColumnNames(String[] columnNames);
    void setColumnNames(String[] columnNames);
    void setColumnTypes(ColumnType[] columnTypes);
    void setSkipColumns(Set<String> skipColumns);
    void setTimeColumnValue(TimeColumnValue timeColumnValue);

    void configure(Task task, TaskResult result) throws PreparePartsException;
    void next(Record row) throws PreparePartsException;

    void writeBeginRow(int size) throws PreparePartsException;
    void writeNil() throws PreparePartsException;
    void write(String v) throws PreparePartsException;
    void write(int v) throws PreparePartsException;
    void write(long v) throws PreparePartsException;
    void write(double v) throws PreparePartsException;
    void write(List<Object> v) throws PreparePartsException;
    void write(Map<Object, Object> v) throws PreparePartsException;
    void writeEndRow() throws PreparePartsException;

    void write(TimeColumnValue filter, StringColumnValue v) throws PreparePartsException;
    void write(TimeColumnValue filter, IntColumnValue v) throws PreparePartsException;
    void write(TimeColumnValue filter, LongColumnValue v) throws PreparePartsException;
    void write(TimeColumnValue filter, DoubleColumnValue v) throws PreparePartsException;

    void validate(TimeColumnValue filter, StringColumnValue v) throws PreparePartsException;
    void validate(TimeColumnValue filter, IntColumnValue v) throws PreparePartsException;
    void validate(TimeColumnValue filter, LongColumnValue v) throws PreparePartsException;
    void validate(TimeColumnValue filter, DoubleColumnValue v) throws PreparePartsException;

    void resetRowNum();
    void incrementRowNum();
    long getRowNum();

    void resetErrorRowNum();
    void incrementErrorRowNum();
    long getErrorRowNum();
}
