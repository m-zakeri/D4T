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

import java.util.logging.Logger;

import com.treasure_data.td_import.prepare.PrepareConfiguration;
import com.treasure_data.td_import.prepare.PreparePartsException;
import com.treasure_data.td_import.writer.RecordWriter;

public class BooleanColumnValue extends AbstractColumnValue {
    private static final Logger LOG = Logger.getLogger(BooleanColumnValue.class.getName());

    private String v;

    public BooleanColumnValue(PrepareConfiguration config, int index, ColumnType columnType) {
        super(config, index, columnType);
    }

    public void set(Object v) throws PreparePartsException {
        this.v = v != null ? v.toString() : "null"; 
    }

    public void parse(String v) throws PreparePartsException {
        this.v = v;
    }

    public String getString() {
        return v;
    }

    @Override
    public void write(RecordWriter with) throws PreparePartsException {
        if (isEmptyString && config.hasEmptyAsNull()) {
            with.writeNil();
            isEmptyString = false;
        } else if (isNullString) {
            with.writeNil();
            isNullString = false;
        } else {
            with.write(v);
        }
    }
}