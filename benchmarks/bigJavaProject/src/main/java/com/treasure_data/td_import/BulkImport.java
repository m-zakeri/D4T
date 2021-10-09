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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.treasure_data.td_import.prepare.MultiThreadPrepareProcessor;
import com.treasure_data.td_import.prepare.PrepareConfiguration;
import com.treasure_data.td_import.source.LocalFileSource;
import com.treasure_data.td_import.source.Source;
import com.treasure_data.td_import.upload.MultiThreadUploadProcessor;
import com.treasure_data.td_import.upload.UploadConfiguration;
import com.treasure_data.td_import.upload.UploadProcessor;
import com.treasuredata.client.TDClient;
import com.treasuredata.client.model.TDBulkImportSession;

import static com.treasuredata.client.model.TDBulkImportSession.ImportStatus.UPLOADING;

public class BulkImport extends Import {
    private static final Logger LOG = Logger.getLogger(BulkImport.class.getName());

    public BulkImport(Properties props) {
        super(props);
    }

    public List<com.treasure_data.td_import.TaskResult<?>> doPrepare(final String[] args)
            throws Exception {
        // create prepare configuration
        PrepareConfiguration prepareConf = createPrepareConf(args);

        // extract and get source names from command-line arguments
        Source[] srcs = getSources(prepareConf, 1);

        MultiThreadPrepareProcessor proc =
                createAndStartPrepareProcessor(prepareConf);

        // create prepare tasks
        com.treasure_data.td_import.prepare.Task[] tasks =
                createPrepareTasks(prepareConf, srcs);

        // start prepare tasks. the method call puts prepare tasks
        startPrepareTasks(prepareConf, tasks);

        // set *finish* tasks on task queue.
        setPrepareFinishTasks(prepareConf);

        // wait for finishing prepare processing
        // extract task results of each prepare processes.
        return stopPrepareProcessor(proc);
    }

    public List<com.treasure_data.td_import.TaskResult<?>> doUpload(final String[] args)
            throws Exception {
        // create configuration for 'upload' processing
        UploadConfiguration uploadConf = createUploadConf(args);

        // create TDClient objects
        TDClient tdClient = uploadConf.createTDClient();

        // configure session name
        TaskResult<?> r = null;
        String sessionName;
        int filePos;
        if (uploadConf.autoCreate()) { // 'auto-create-session'
            // create session automatically
            sessionName = createBulkImportSessionName(uploadConf, tdClient);

            filePos = 1;
        } else {
            // get session name from command-line arguments
            sessionName = getBulkImportSessionName(uploadConf);

            // validate that the session is live or not
            r = UploadProcessor.checkSession(tdClient, sessionName);
            if (r.error != null) {
                throw new IllegalArgumentException(r.error);
            }

            filePos = 2;
        }

        // if session is already freezed, exception is thrown.
        TDBulkImportSession sess = UploadProcessor.showSession(tdClient, sessionName);
        if (sess.isUploadFrozen()) {
            throw new IllegalArgumentException(String.format(
                    "Bulk import session %s is already freezed. Please check it with 'td import:show %s'",
                    sessionName, sessionName));
        }

        // get and extract uploaded sources from command-line arguments
        Source[] srcs = getSources(uploadConf, filePos);

        List<com.treasure_data.td_import.TaskResult<?>> results =
                new ArrayList<com.treasure_data.td_import.TaskResult<?>>();
        boolean hasNoPrepareError = true;
        MultiThreadUploadProcessor uploadProc =
                createAndStartUploadProcessor(uploadConf);
        try {
            if (!uploadConf.hasPrepareOptions()) {
                // create upload tasks
                com.treasure_data.td_import.upload.UploadTask[] tasks =
                        createUploadTasks(sessionName, srcs);

                // start upload tasks. the method call puts upload tasks
                startUploadTasks(uploadConf, tasks);
            } else {
                // create configuration for 'prepare' processing
                PrepareConfiguration prepareConf = createPrepareConf(args, true);

                MultiThreadPrepareProcessor prepareProc =
                        createAndStartPrepareProcessor(prepareConf);

                // create sequential upload (prepare) tasks
                com.treasure_data.td_import.prepare.Task[] tasks =
                        createSequentialUploadTasks(sessionName, srcs);

                // start sequential upload (prepare) tasks. the method call puts
                // prepare tasks
                startPrepareTasks(prepareConf, tasks);

                // set *finish* prepare tasks on prepare task queue.
                // after those prepare tasks are finished, automatically the
                // upload tasks are put on upload task queue.
                setPrepareFinishTasks(prepareConf);

                // wait for finishing all prepare tasks by using *finish*
                // prepare tasks.
                results.addAll(stopPrepareProcessor(prepareProc));

                hasNoPrepareError = hasNoPrepareError(results);
            }
        } finally {
            // put *finish* upload tasks on upload task queue
            setUploadFinishTasks(uploadConf);
        }

        // wait for finishing all upload tasks by using *finish* tasks.
        results.addAll(stopUploadProcessor(uploadProc));

        if (!hasNoUploadError(results) || !hasNoPrepareError) {
            return results;
        }

        // 'auto-perform' and 'auto-commit'
        results.add(UploadProcessor.processAfterUploading(tdClient, uploadConf, sessionName));

        // 'auto-delete'
        if (hasNoUploadError(results) && uploadConf.autoDelete() && uploadConf.autoCommit()) {
            results.add(UploadProcessor.deleteSession(tdClient, sessionName));
        }

        return results;
    }

    protected com.treasure_data.td_import.prepare.Task[] createPrepareTasks(
            final PrepareConfiguration conf,
            final Source[] sources) {
        com.treasure_data.td_import.prepare.Task[] tasks =
                new com.treasure_data.td_import.prepare.Task[sources.length];
        for (int i = 0; i < sources.length; i++) {
            tasks[i] = new com.treasure_data.td_import.prepare.Task(sources[i]);
        }
        return tasks;
    }

    protected com.treasure_data.td_import.prepare.Task[] createSequentialUploadTasks(
            final String sessionName,
            final Source[] sources) {
        com.treasure_data.td_import.prepare.Task[] tasks =
                new com.treasure_data.td_import.prepare.Task[sources.length];
        for (int i = 0; i < sources.length; i++) {
            tasks[i] = new com.treasure_data.td_import.prepare.SequentialUploadTask(
                    sessionName, sources[i]);
        }
        return tasks;
    }

    protected com.treasure_data.td_import.upload.UploadTask[] createUploadTasks(
            final String sessionName,
            final Source[] sources) {
        com.treasure_data.td_import.upload.UploadTask[] tasks =
                new com.treasure_data.td_import.upload.UploadTask[sources.length];
        for (int i = 0; i < sources.length; i++) {
            tasks[i] = new com.treasure_data.td_import.upload.UploadTask(
                    sessionName, (LocalFileSource) sources[i]);
        }
        return tasks;
    }

    protected void startUploadTasks(final UploadConfiguration conf,
            final com.treasure_data.td_import.upload.UploadTask[] tasks) {
        for (int i = 0; i < tasks.length; i++) {
            try {
                MultiThreadUploadProcessor.addTask(tasks[i]);
            } catch (Throwable t) {
                LOG.log(Level.SEVERE, "error occurred during 'addTask' method call", t);
            }
        }
    }

    protected void setUploadFinishTasks(final UploadConfiguration conf) {
        // put *finish* upload tasks on upload task queue
        try {
            MultiThreadUploadProcessor.addFinishTask(conf);
        } catch (Throwable t) {
            LOG.log(Level.SEVERE, "error occurred during 'addFinishTask' method call", t);
        }
    }

    protected String createBulkImportSessionName(UploadConfiguration conf, TDClient tdClient) throws Exception {
        String databaseName = conf.enableMake()[0];
        String tableName = conf.enableMake()[1];
        Date d = new Date();
        String format = "yyyy_MM_dd";
        String timestamp = new SimpleDateFormat(format).format(d);
        String sessionName = String.format("%s_%s_%s_%d",
                databaseName, tableName, timestamp, d.getTime());

        TaskResult<?> e = null;

        // create bulk import session
        e = UploadProcessor.createSession(tdClient, sessionName, databaseName, tableName);
        if (e.error != null) {
            throw new IllegalArgumentException(e.error);
        }

        return sessionName;
    }

    protected String getBulkImportSessionName(UploadConfiguration conf) {
        List<String> argList = conf.getNonOptionArguments();
        if (argList.size() < 1) {
            throw new IllegalArgumentException("Session name not specified");
        }
        return argList.get(1);
    }

    protected PrepareConfiguration createPrepareConf(String[] args) {
        return createPrepareConf(args, false);
    }

    protected PrepareConfiguration createPrepareConf(String[] args, boolean isUploaded) {
        PrepareConfiguration.Factory fact = new PrepareConfiguration.Factory(props, isUploaded);
        PrepareConfiguration conf = fact.newPrepareConfiguration(args);

        if (!isUploaded) {
            showHelp(Configuration.Command.PREPARE, conf, args);
        }

        conf.configure(props, fact.getBulkImportOptions());
        return conf;
    }

    protected UploadConfiguration createUploadConf(String[] args) {
        UploadConfiguration.Factory fact = new UploadConfiguration.Factory(props);
        UploadConfiguration conf = fact.newUploadConfiguration(args);

        showHelp(Configuration.Command.UPLOAD, conf, args);

        conf.configure(props, fact.getBulkImportOptions());
        return conf;
    }
}
