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


import com.treasure_data.td_import.prepare.PrepareConfiguration;

public abstract class AbstractColumnValue implements ColumnValue {
    private static final String NULL_LOWERCASE = "null";
    private static final String NULL_UPPERCASE = "NULL";
    private static final String NIL_LOWERCASE = "nil";
    private static final String NIL_UPPERCASE = "NIL";

    protected static boolean isNullString(String v) {
        return v.equals(NULL_LOWERCASE) || v.equals(NULL_UPPERCASE)
                || v.equals(NIL_LOWERCASE) || v.equals(NIL_UPPERCASE);
    }

    protected boolean isNullString = false;
    protected boolean isEmptyString = false;

    protected PrepareConfiguration config;
    protected int index;
    protected ColumnType columnType;

    public AbstractColumnValue(PrepareConfiguration config, int index, ColumnType columnType) {
        this.config = config;
        this.index = index;
        this.columnType = columnType;
    }

    public ColumnType getColumnType() {
        return columnType;
    }
}