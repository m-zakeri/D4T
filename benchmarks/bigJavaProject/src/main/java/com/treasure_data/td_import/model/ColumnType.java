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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.treasure_data.td_import.prepare.PrepareConfiguration;
import com.treasure_data.td_import.prepare.PreparePartsException;
import com.treasure_data.td_import.writer.RecordWriter;

public interface ColumnType {

    ColumnType NIL = new NilColumnType(); // special column type for JSON files
    ColumnType BOOLEAN = new BooleanColumnType();
    ColumnType DOUBLE = new DoubleColumnType();
    ColumnType FLOAT = new FloatColumnType();
    ColumnType INT = new IntColumnType();
    ColumnType BIGINT = new BigIntColumnType();
    ColumnType LONG = new LongColumnType();
    ColumnType STRING = new StringColumnType();
    ColumnType ARRAY = new ArrayColumnType();
    ColumnType MAP = new MapColumnType();

    String getName();
    int getOrderIndex();

    ColumnValue createColumnValue(PrepareConfiguration config, int index);
    void convertType(String v, ColumnValue into) throws PreparePartsException;
    void setColumnValue(Object v, ColumnValue cv) throws PreparePartsException;
    void filterAndWrite(ColumnValue v, TimeColumnValue filter, RecordWriter with) throws PreparePartsException;
    void filterAndValidate(ColumnValue v, TimeColumnValue filter, RecordWriter with) throws PreparePartsException;

    public static class Conv {
        private static final Map<Integer, ColumnType> REVERSE_INTS = new HashMap<Integer, ColumnType>();
        private static final Map<String, ColumnType> REVERSE_STRINGS = new HashMap<String, ColumnType>();

        static {
            for (ColumnType t : Arrays.asList(BOOLEAN, DOUBLE, FLOAT, INT, BIGINT, LONG, STRING, ARRAY, MAP)) {
                REVERSE_INTS.put(t.getOrderIndex(), t);
                REVERSE_STRINGS.put(t.getName(), t);
            }
        }

        public static ColumnType fromInt(int index) {
            return REVERSE_INTS.get(index);
        }

        public static ColumnType fromString(String name) {
            return REVERSE_STRINGS.get(name);
        }
    }
}
