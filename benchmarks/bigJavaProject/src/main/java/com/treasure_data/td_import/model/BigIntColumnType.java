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
import com.treasure_data.td_import.prepare.PreparePartsException;
import com.treasure_data.td_import.writer.RecordWriter;

public class BigIntColumnType extends AbstractColumnType {

    protected BigIntColumnType() {
        super("bigint", 3);
    }

    public ColumnValue createColumnValue(PrepareConfiguration config, int index) {
        throw new UnsupportedOperationException();
    }

    public void convertType(String v, ColumnValue into) throws PreparePartsException {
        throw new UnsupportedOperationException();
    }

    public void setColumnValue(Object v, ColumnValue cv) throws PreparePartsException {
        throw new UnsupportedOperationException();
    }

    public void filterAndWrite(ColumnValue v, TimeColumnValue filter, RecordWriter with)
            throws PreparePartsException {
        throw new UnsupportedOperationException("This method is not needed");
    }

    public void filterAndValidate(ColumnValue v, TimeColumnValue filter, RecordWriter with)
            throws PreparePartsException {
        throw new UnsupportedOperationException("This method is not needed");
    }
}