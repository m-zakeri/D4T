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
import com.treasure_data.td_import.reader.SyslogRecordReader;

public class SyslogPrepareConfiguration extends RegexPrepareConfiguration {
    private static final Logger LOG = Logger
            .getLogger(SyslogPrepareConfiguration.class.getName());

    public SyslogPrepareConfiguration() {
    }

    public void configure(Properties props, Options options) {
        super.configure(props, options);
    }

    @Override
    public void setRegexPattern() {
        regexPattern = SyslogRecordReader.syslogPatString;
    }

    @Override
    public void setColumnNames() {
        columnNames = new String[] { "time", "host", "ident", "pid", "message" };
        actualColumnNames = new String[] { "time", "host", "ident", "pid", "message" };
    }

    @Override
    public void setColumnTypes() {
        columnTypes = new ColumnType[] { ColumnType.STRING, ColumnType.STRING,
                ColumnType.STRING, ColumnType.INT, ColumnType.STRING, };
    }
}
