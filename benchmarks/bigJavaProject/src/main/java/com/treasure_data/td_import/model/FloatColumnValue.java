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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.treasure_data.td_import.prepare.PrepareConfiguration;
import com.treasure_data.td_import.prepare.PreparePartsException;
import com.treasure_data.td_import.writer.RecordWriter;

public class FloatColumnValue extends AbstractColumnValue {
    private static final Logger LOG = Logger.getLogger(FloatColumnValue.class.getName());

    private float v;

    public FloatColumnValue(PrepareConfiguration config, int index, ColumnType columnType) {
        super(config, index, columnType);
    }

    public void set(Object v) throws PreparePartsException {
        this.v = v != null ? (Float) v : 0;
    }

    public void parse(String v) throws PreparePartsException {
        if (isEmptyString = (v == null)) {
            this.v = 0;
            return;
        }

        if (isNullString = isNullString(v)) {
            this.v = 0;
            return;
        }

        try {
            this.v = Float.parseFloat(v);
        } catch (NumberFormatException e) {
            String cause = "Column[" + index + "] value cannot be converted to Float type: " + e.getMessage();
            throw new PreparePartsException(cause, e);
        } catch (Exception e) {
            LOG.log(Level.WARNING, String.format(
                    "Cannot parse '%s' to float type", v), e);
            throw new PreparePartsException(e);
        }
    }

    public float getFloat() {
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