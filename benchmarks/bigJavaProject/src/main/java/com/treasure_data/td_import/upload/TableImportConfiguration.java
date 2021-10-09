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
package com.treasure_data.td_import.upload;

import java.util.Properties;

import com.treasure_data.td_import.Options;
import com.treasure_data.td_import.Configuration;

public class TableImportConfiguration extends UploadConfigurationBase {

    public static class Factory {
        protected Options options;

        public Factory(Properties props) {
            options = new Options();
            options.initTableImportOptionParser(props);
        }

        public Options getTableImportOptions() {
            return options;
        }

        public TableImportConfiguration newUploadConfiguration(String[] args) {
            options.setOptions(args);
            TableImportConfiguration c = new TableImportConfiguration();
            c.options = options;
            return c;
        }
    }

    protected boolean autoCreateTable = false;

    public TableImportConfiguration() {
        super();
    }

    @Override
    public UploadProcessorBase createNewUploadProcessor() {
        return new ImportProcessor(createTDClient(), this);
    }

    @Override
    public String showHelp(Properties props) {
        StringBuilder sbuf = new StringBuilder();

        // usage
        sbuf.append("usage:\n");
        sbuf.append(Configuration.CMD_TABLEIMPORT_USAGE);
        sbuf.append("\n");

        // example
        sbuf.append("example:\n");
        sbuf.append(Configuration.CMD_TABLEIMPORT_EXAMPLE);
        sbuf.append("\n");

        // description
        sbuf.append("description:\n");
        sbuf.append(Configuration.CMD_TABLEIMPORT_DESC);
        sbuf.append("\n");

        // options
        sbuf.append("options:\n");
        sbuf.append(Configuration.CMD_TABLEIMPORT_OPTIONS);
        sbuf.append("\n");

        return sbuf.toString();
    }

    public void configure(Properties props, Options options) {
        super.configure(props, options);

        // 'format' is checked in super.configuration(..)
        setFormat();

        // time-key

        // auto-create-table
        setAutoCreateTable();
    }

    @Override
    public void setFormat() {
        String formatStr;
        if (optionSet.has(BI_PREPARE_PARTS_FORMAT)) {
            formatStr = (String) optionSet.valueOf(BI_PREPARE_PARTS_FORMAT);
            format = Format.fromString(formatStr);
            if (format == null) {
                throw new IllegalArgumentException(String.format(
                        "unsupported format '%s'", formatStr));
            }
        }

        if (optionSet.has(TABLE_IMPORT_FORMAT_APACHE)) {
            format = Format.APACHE;
        }

        if (optionSet.has(TABLE_IMPORT_FORMAT_SYSLOG)) {
            format = Format.SYSLOG;
        }

        if (optionSet.has(TABLE_IMPORT_FORMAT_MSGPACK)) {
            format = Format.MSGPACK;
        }

        if (optionSet.has(TABLE_IMPORT_FORMAT_JSON)) {
            format = Format.JSON;
        }

        throw new IllegalArgumentException("Not specified format");
    }

    public void setAutoCreateTable() {
        autoCreateTable = optionSet.has(TABLE_IMPORT_AUTO_CREATE_TABLE);
    }

    public Object clone() {
        TableImportConfiguration conf = new TableImportConfiguration();
        conf.props = props;
        conf.autoCreateTable = autoCreateTable;
        conf.numOfUploadThreads = numOfUploadThreads;
        conf.retryCount= retryCount;
        conf.waitSec = waitSec;
        return conf;
    }
}
