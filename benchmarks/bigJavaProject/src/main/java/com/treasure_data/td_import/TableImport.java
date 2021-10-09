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
package com.treasure_data.td_import;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import com.treasure_data.td_import.prepare.MultiThreadPrepareProcessor;
import com.treasure_data.td_import.prepare.PrepareConfiguration;
import com.treasure_data.td_import.source.Source;
import com.treasure_data.td_import.upload.MultiThreadUploadProcessor;
import com.treasure_data.td_import.upload.TableImportConfiguration;

@Deprecated
public class TableImport extends Import {
    private static final Logger LOG = Logger.getLogger(TableImport.class.getName());

    public TableImport(Properties props) {
        super(props);
    }

    public List<com.treasure_data.td_import.TaskResult<?>> tableImport(final String[] args)
            throws Exception {
        // create configuration for 'table:import' processing
        TableImportConfiguration importConf =
                createTableImportConfiguration(args);

        TaskResult<?> r = null;
        String databaseName = getDatabaseName(importConf); // database exists? TODO
        String tableName = getTableName(importConf); // table exists? TODO

        // get and extract uploaded sources from command-line arguments
        Source[] srcs = getSources(importConf, 2);

        MultiThreadUploadProcessor uploadProc =
                createAndStartUploadProcessor(importConf);

        List<com.treasure_data.td_import.TaskResult<?>> results =
                new ArrayList<com.treasure_data.td_import.TaskResult<?>>();

        // create configuration for 'prepare' processing
        // TODO final PrepareConfiguration prepareConf = createPrepareConfiguration(args, true);

        MultiThreadPrepareProcessor prepareProc =
                createAndStartPrepareProcessor(importConf);

        // create sequential upload (prepare) tasks
        com.treasure_data.td_import.prepare.Task[] tasks =
                createSequentialImportTasks(databaseName, tableName, srcs);

        // start sequential upload (prepare) tasks
        startPrepareTasks(importConf, tasks);

        setPrepareFinishTasks(importConf);

        results.addAll(stopPrepareProcessor(prepareProc));
        if (!hasNoPrepareError(results)) {
            return results;
        }

        // end of file list
        try {
            MultiThreadUploadProcessor.addFinishTask(importConf);
        } catch (Throwable t) {
            LOG.severe("Error occurred During 'addFinishTask' method call");
            LOG.throwing("Main", "addFinishTask", t);
        }

        results.addAll(stopUploadProcessor(uploadProc));
        if (!hasNoUploadError(results)) {
            return results;
        }

        return results;
    }

    protected String getDatabaseName(PrepareConfiguration conf) {
        return conf.getNonOptionArguments().get(0);
    }

    protected String getTableName(PrepareConfiguration conf) {
        return conf.getNonOptionArguments().get(1);
    }

    protected com.treasure_data.td_import.prepare.Task[] createSequentialImportTasks(
            final String databaseName, final String tableName, final Source[] sources) {
        com.treasure_data.td_import.prepare.Task[] tasks =
                new com.treasure_data.td_import.prepare.Task[sources.length];
        for (int i = 0; i < sources.length; i++) {
            tasks[i] = new com.treasure_data.td_import.prepare.SequentialImportTask(
                    databaseName, tableName, sources[i]);
        }
        return tasks;
    }

    protected TableImportConfiguration createTableImportConfiguration(String[] args) {
        TableImportConfiguration.Factory fact = new TableImportConfiguration.Factory(props);
        TableImportConfiguration conf = fact.newUploadConfiguration(args);

        showHelp(Configuration.Command.TABLEIMPORT, conf, args);

        conf.configure(props, fact.getTableImportOptions());
        return conf;
    }

    public static void main(final String[] args) throws Exception {
        Properties props = System.getProperties();
        new TableImport(props).tableImport(args);
    }
}
