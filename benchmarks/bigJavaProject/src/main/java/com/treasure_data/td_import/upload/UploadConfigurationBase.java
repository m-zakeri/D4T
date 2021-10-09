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
import com.treasure_data.td_import.prepare.PrepareConfiguration;
import com.treasuredata.client.TDClient;
import com.treasuredata.client.TDClientBuilder;

public class UploadConfigurationBase extends PrepareConfiguration {
    protected int numOfUploadThreads;
    protected int retryCount;
    protected long waitSec;

    public UploadConfigurationBase() {
        super();
    }

    public UploadProcessorBase createNewUploadProcessor() {
        throw new UnsupportedOperationException();
    }

    public TDClient createTDClient() {
        Properties props = getProperties();
        TDClientBuilder builder = TDClient.newBuilder();
        builder.setProperties(props);

        return builder.build();
    }

    public void configure(Properties props, Options options) {
        super.configure(props, options);

        // retry-count
        setRetryCount();

        // parallel
        setNumOfUploadThreads();

        // waitSec
        String wsec = props.getProperty(BI_UPLOAD_PARTS_WAITSEC,
                BI_UPLOAD_PARTS_WAITSEC_DEFAULTVALUE);
        try {
            waitSec = Long.parseLong(wsec);
        } catch (NumberFormatException e) {
            String msg = String.format(
                    "'long' value is required as 'wait sec' e.g. -D%s=5",
                    BI_UPLOAD_PARTS_WAITSEC);
            throw new IllegalArgumentException(msg, e);
        }
    }

    public Properties getProperties() {
        return props;
    }

    public boolean hasPrepareOptions() {
        return optionSet.has(BI_PREPARE_PARTS_FORMAT)
                || optionSet.has(BI_PREPARE_PARTS_COMPRESSION)
                || optionSet.has(BI_PREPARE_PARTS_PARALLEL)
                || optionSet.has(BI_PREPARE_PARTS_ENCODING)
                || optionSet.has(BI_PREPARE_PARTS_COLUMNHEADER)               // column-header
                || optionSet.has(BI_PREPARE_PARTS_TIMECOLUMN)
                || optionSet.has(BI_PREPARE_PARTS_TIMEFORMAT)
                || optionSet.has(BI_PREPARE_PARTS_TIMEVALUE)
                || optionSet.has(BI_PREPARE_PARTS_OUTPUTDIR)
                || optionSet.has(BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING)
                || optionSet.has("dry-run")
                || optionSet.has(BI_PREPARE_PARTS_SPLIT_SIZE)
                || optionSet.has(BI_PREPARE_PARTS_COLUMNS)
                || optionSet.has(BI_PREPARE_PARTS_COLUMNTYPES)
                || optionSet.has(BI_PREPARE_COLUMNTYPE)                       // column-type
                || optionSet.has(BI_PREPARE_ALL_STRING)                       // all-string
                || optionSet.has(BI_PREPARE_PARTS_EMPTYASNULL)                // empty-as-null-if-numeric
                || optionSet.has(BI_PREPARE_PARTS_ERROR_RECORDS_OUTPUT)       // error-records-output
                || optionSet.has(BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING)     // error-records-handling
                || optionSet.has(BI_PREPARE_INVALID_COLUMNS_HANDLING)         // invalid-columns-handling
                || optionSet.has(BI_PREPARE_PARTS_EXCLUDE_COLUMNS)
                || optionSet.has(BI_PREPARE_PARTS_ONLY_COLUMNS);
    }

    public void setNumOfUploadThreads() {
        numOfUploadThreads = 2;
    }

    public int getNumOfUploadThreads() {
        return numOfUploadThreads;
    }

    public void setRetryCount() {
        String num;
        if (!optionSet.has(BI_UPLOAD_RETRY_COUNT)) {
            num = BI_UPLOAD_RETRY_COUNT_DEFAULTVALUE;
        } else {
            num = (String) optionSet.valueOf(BI_UPLOAD_RETRY_COUNT);
        }
        try {
            retryCount = Integer.parseInt(num);
        } catch (NumberFormatException e) {
            String msg = "retry-count option requires 'int' value";
            throw new IllegalArgumentException(msg, e);
        }
    }

    public int getRetryCount() {
        return retryCount;
    }

    public long getWaitSec() {
        return waitSec;
    }

}
