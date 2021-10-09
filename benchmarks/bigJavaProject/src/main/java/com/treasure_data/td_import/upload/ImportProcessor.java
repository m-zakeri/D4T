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

import java.io.File;
import java.util.logging.Logger;

import com.treasuredata.client.TDClient;
import com.treasuredata.client.TDClientException;

public class ImportProcessor extends UploadProcessorBase {

    private static final Logger LOG = Logger.getLogger(ImportProcessor.class.getName());

    protected TDClient client;

    public ImportProcessor(TDClient client, UploadConfigurationBase conf) {
        super(conf);
        this.client = client;
    }

    public TaskResult execute(final UploadTaskBase base) {
        final ImportTask task = (ImportTask) base;
        TaskResult result = new TaskResult();
        result.task = task;
        long time = System.currentTimeMillis();

        try {
            System.out.println(String.format("Importing %s (%d bytes)...", task.fileName, task.size));
            LOG.info(String.format("Importing %s (%d bytes) to %s.%s",
                    task.fileName, task.size, task.databaseName, task.tableName));

            client.uploadBulkImportPart(task.databaseName, task.tableName, new File(task.fileName));
            time = System.currentTimeMillis() - time;
            task.finishHook(task.fileName);

            LOG.info(String.format(
                    "Imported file %s (%d bytes) to %s.%s (time: %d sec.)",
                    task.fileName, task.size, task.databaseName, task.tableName, (time / 1000)));
        } catch (TDClientException e) {
            LOG.severe(e.getMessage());
            result.error = e;
        }

        return result;
    }
}
