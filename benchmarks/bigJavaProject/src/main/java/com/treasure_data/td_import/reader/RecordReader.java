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

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;

import com.treasure_data.td_import.model.ColumnType;
import com.treasure_data.td_import.model.TimeColumnSampling;
import com.treasure_data.td_import.model.TimeColumnValue;
import com.treasure_data.td_import.prepare.PrepareConfiguration;
import com.treasure_data.td_import.prepare.PreparePartsException;
import com.treasure_data.td_import.prepare.Task;

public interface RecordReader<T extends PrepareConfiguration> extends Closeable {

    void configure(Task task) throws PreparePartsException;

    void resetLineNum();
    void incrementLineNum();
    long getLineNum();

    String[] getActualColumnNames();
    String[] getColumnNames();
    ColumnType[] getColumnTypes();
    Set<String> getSkipColumns();
    void setSkipColumns();

    int getTimeColumnIndex();
    int getAliasTimeColumnIndex(int timeColumnIndex);
    TimeColumnValue getTimeColumnValue();
    void setTimeColumnValue(TimeColumnSampling[] sampleColumnValues,
            int timeColumnIndex, int aliasTimeColumnIndex);

    void initializeColumnTypes(TimeColumnSampling[] sampleColumnValues);
    void initializeWrittenRecord();

    boolean next() throws PreparePartsException;

    boolean readRecord() throws IOException, PreparePartsException;

    void convertTypes() throws PreparePartsException;

    // get latest untokenized record
    String getCurrentRecord();

    // handle PreparePartsException
    void handleError(PreparePartsException e) throws PreparePartsException;

    // write untokenized records that are invalid records to err-records.txt
    void writeErrorRecord(String record);
}
