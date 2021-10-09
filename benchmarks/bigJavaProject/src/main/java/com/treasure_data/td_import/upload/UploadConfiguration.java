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
import com.treasuredata.client.TDClient;

public class UploadConfiguration extends UploadConfigurationBase {

    public static class Factory {
        protected Options options;

        public Factory(Properties props) {
            options = new Options();
            options.initUploadOptionParser(props);
        }

        public Options getBulkImportOptions() {
            return options;
        }

        public UploadConfiguration newUploadConfiguration(String[] args) {
            options.setOptions(args);
            UploadConfiguration c = new UploadConfiguration();
            c.options = options;
            return c;
        }
    }

    protected boolean autoCreate = false;
    protected String[] enableMake = null;
    protected boolean autoPerform = false;
    protected boolean autoCommit = false;
    protected boolean autoDelete = false;
    protected long waitSec;

    public UploadConfiguration() {
        super();
    }

    @Override
    public UploadProcessorBase createNewUploadProcessor() {
        TDClient c = createTDClient();
        return new UploadProcessor(c, this);
    }

    @Override
    public String showHelp(Properties props) {
        boolean isAuto = Boolean.parseBoolean(props.getProperty(CMD_AUTO_ENABLE, "false"));

        StringBuilder sbuf = new StringBuilder();

        // usage
        sbuf.append("usage:\n");
        if (isAuto) {
            sbuf.append(Configuration.CMD_AUTO_USAGE);
        } else {
            sbuf.append(Configuration.CMD_UPLOAD_USAGE);
        }
        sbuf.append("\n");

        // example
        sbuf.append("example:\n");
        if (isAuto) {
            sbuf.append(Configuration.CMD_AUTO_EXAMPLE);
        } else {
            sbuf.append(Configuration.CMD_UPLOAD_EXAMPLE);
        }
        sbuf.append("\n");

        // description
        sbuf.append("description:\n");
        if (isAuto) {
            sbuf.append(Configuration.CMD_AUTO_DESC);
        } else {
            sbuf.append(Configuration.CMD_UPLOAD_DESC);
        }
        sbuf.append("\n");

        // options
        sbuf.append("options:\n");
        if (isAuto) {
            sbuf.append(Configuration.CMD_AUTO_OPTIONS);
        } else {
            sbuf.append(Configuration.CMD_UPLOAD_OPTIONS);
        }
        sbuf.append("\n");

        return sbuf.toString();
    }

    public void configure(Properties props, Options options) {
        super.configure(props, options);

        // auto-create-session
        setAutoCreate();

        // auto-perform
        setAutoPerform();

        // auto-commit
        setAutoCommit();

        // auto-delete-session
        setAutoDelete();
    }

    public void setAutoPerform() {
        autoPerform = optionSet.has(BI_UPLOAD_PARTS_AUTO_PERFORM);
    }

    public boolean autoPerform() {
        return autoPerform;
    }

    public void setAutoCommit() {
        autoCommit = optionSet.has(BI_UPLOAD_PARTS_AUTO_COMMIT);
    }

    public boolean autoCommit() {
        return autoCommit;
    }

    public void setAutoCreate() {
        if (optionSet.has(BI_UPLOAD_PARTS_AUTO_CREATE)) {
            autoCreate = true;
            enableMake = optionSet.valuesOf(BI_UPLOAD_PARTS_AUTO_CREATE).toArray(new String[0]);
            if (enableMake.length != 2) {
                throw new IllegalArgumentException(String.format(
                        "'%s' option argument must consists of database and table names e.g. 'testdb:testtbl'",
                        BI_UPLOAD_PARTS_AUTO_CREATE));
            }
        }
    }

    public boolean autoCreate() {
        return autoCreate;
    }

    public String[] enableMake() {
        return enableMake;
    }

    public void setAutoDelete() {
        boolean ad = optionSet.has(BI_UPLOAD_PARTS_AUTO_DELETE);
        if (ad) {
            if (autoCommit) {
                autoDelete = ad;
            } else {
                throw new IllegalArgumentException(String.format(
                        "'%s' option cannot be used without '%s' option.",
                        BI_UPLOAD_PARTS_AUTO_DELETE, BI_UPLOAD_PARTS_AUTO_COMMIT));
            }
        }
    }

    public boolean autoDelete() {
        return autoDelete;
    }

    @Override
    public void setNumOfUploadThreads() {
        String num;
        if (!optionSet.has(BI_UPLOAD_PARTS_PARALLEL)) {
            num = BI_UPLOAD_PARTS_PARALLEL_DEFAULTVALUE;
        } else {
            num = (String) optionSet.valueOf(BI_UPLOAD_PARTS_PARALLEL);
        }

        try {
            int n = Integer.parseInt(num);
            if (n < 0) {
                numOfUploadThreads = 2;
            } else if (n > 9){
                numOfUploadThreads = 8;
            } else {
                numOfUploadThreads = n;
            }
        } catch (NumberFormatException e) {
            String msg = String.format(
                    "'int' value is required as '%s' option",
                    BI_UPLOAD_PARTS_PARALLEL);
            throw new IllegalArgumentException(msg, e);
        }
    }

    public long getWaitSec() {
        return waitSec;
    }

    public Object clone() {
        UploadConfiguration conf = new UploadConfiguration();
        conf.props = props;
        conf.autoCreate = autoCreate;
        conf.autoPerform = autoPerform;
        conf.autoCommit = autoCommit;
        conf.autoDelete = autoDelete;
        conf.numOfUploadThreads = numOfUploadThreads;
        conf.retryCount= retryCount;
        conf.waitSec = waitSec;
        return conf;
    }
}
