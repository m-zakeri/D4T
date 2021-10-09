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
package com.treasure_data.td_import.prepare;

import java.util.Properties;
import java.util.logging.Logger;

import com.treasure_data.td_import.Options;
import com.treasure_data.td_import.model.ColumnType;
import com.treasure_data.td_import.model.ColumnValue;
import com.treasure_data.td_import.model.IntColumnType;
import com.treasure_data.td_import.model.IntColumnValue;
import com.treasure_data.td_import.model.LongColumnType;
import com.treasure_data.td_import.model.LongColumnValue;
import com.treasure_data.td_import.reader.ApacheRecordReader;

public class ApachePrepareConfiguration extends RegexPrepareConfiguration {
    private static final Logger LOG = Logger
            .getLogger(ApachePrepareConfiguration.class.getName());

    public static class ApacheLongColumnType extends LongColumnType {
        public ApacheLongColumnType() {
            super();
        }

        @Override
        public ColumnValue createColumnValue(PrepareConfiguration config, int index) {
            return new LongColumnValue(config, index, this);
        }

        @Override
        public void convertType(String v, ColumnValue into)
                throws PreparePartsException {
            if (v.equals("-")) {
                into.parse("0");
            } else {
                into.parse(v);
            }
        }
    }

    public static class ApacheIntColumnType extends IntColumnType {
        public ApacheIntColumnType() {
            super();
        }

        @Override
        public ColumnValue createColumnValue(PrepareConfiguration config, int index) {
            return new IntColumnValue(config, index, this);
        }

        @Override
        public void convertType(String v, ColumnValue into)
                throws PreparePartsException {
            if (v.equals("-")) {
                into.parse("0");
            } else {
                into.parse(v);
            }
        }
    }

    public ApachePrepareConfiguration() {
    }

    public void configure(Properties props, Options options) {
        super.configure(props, options);
    }

    @Override
    public void setRegexPattern() {
        regexPattern = ApacheRecordReader.commonLogPatString;
    }

    @Override
    public void setColumnNames() {
        columnNames = new String[] { "host", "user", "time", "method", "path",
                "code", "size", "referer", "agent" };
        actualColumnNames = new String[] { "host", "user", "time", "method", "path",
                "code", "size", "referer", "agent" };
    }

    @Override
    public void setColumnTypes() {
        columnTypes = new ColumnType[] { ColumnType.STRING, ColumnType.STRING,
                ColumnType.STRING, ColumnType.STRING, ColumnType.STRING,
                new ApacheIntColumnType(), new ApacheLongColumnType(), ColumnType.STRING,
                ColumnType.STRING, };
    }
}
