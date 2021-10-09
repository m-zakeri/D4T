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
package com.treasure_data.td_import.model;

import com.treasure_data.td_import.Configuration;
import com.treasure_data.td_import.prepare.PreparePartsException;
import com.treasure_data.td_import.prepare.Strftime;
import com.treasure_data.td_import.writer.RecordWriter;

public class TimeColumnValue {
    protected int index;
    protected Strftime timeFormat;

    public TimeColumnValue(int index, Strftime timeFormat) {
        this.index = index;
        this.timeFormat = timeFormat;
    }

    public int getIndex() {
        return index;
    }

    public Strftime getTimeFormat() {
        return timeFormat;
    }

    public void write(ColumnValue v, RecordWriter with) throws PreparePartsException {
        v.getColumnType().filterAndWrite(v, this, with);
    }

    public void validate(ColumnValue v, RecordWriter with) throws PreparePartsException {
        v.getColumnType().filterAndValidate(v, this, with);
    }

    public void validateUnixtime(int v) throws PreparePartsException {
        validateUnixtime((long) v);
    }

    public void validateUnixtime(long v) throws PreparePartsException {
        if (v > Configuration.MAX_LOG_TIME) {
            throw new PreparePartsException(String.format(
                    "values of 'time' column must be less than 9999/12/31: %d", v));
        }
    }
}