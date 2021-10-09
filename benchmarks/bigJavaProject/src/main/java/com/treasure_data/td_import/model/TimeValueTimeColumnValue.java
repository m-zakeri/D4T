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

import com.treasure_data.td_import.prepare.PreparePartsException;
import com.treasure_data.td_import.writer.RecordWriter;

public class TimeValueTimeColumnValue extends TimeColumnValue {
    private final long timeValue;
    private final long hours;
    private long currentTimeValue;

    public TimeValueTimeColumnValue(long timeValue) {
        this(timeValue, 0);
    }

    public TimeValueTimeColumnValue(long timeValue, long hours) {
        super(0, null);
        this.timeValue = timeValue;
        this.currentTimeValue = timeValue;
        this.hours = hours * 60 * 60;
    }

    public long getTimeValue() {
        return timeValue;
    }

    public void write(RecordWriter with) throws PreparePartsException {
        with.write(currentTimeValue);
        if (hours > 0) {
            currentTimeValue++;
            if (currentTimeValue > timeValue + hours) {
                currentTimeValue = timeValue;
            }
        }
    }

    public void write(ColumnValue v, RecordWriter with) throws PreparePartsException {
        write(with); // v is ignore
    }

    public void validate(ColumnValue v, RecordWriter with) throws PreparePartsException {
        validateUnixtime(timeValue);
    }
}