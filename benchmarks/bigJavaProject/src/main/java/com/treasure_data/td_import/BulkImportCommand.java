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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.treasure_data.td_import.prepare.MultiThreadPrepareProcessor;
import com.treasure_data.td_import.prepare.PrepareConfiguration;
import com.treasure_data.td_import.source.Source;
import com.treasure_data.td_import.upload.MultiThreadUploadProcessor;
import com.treasure_data.td_import.upload.UploadConfiguration;
import com.treasure_data.td_import.upload.UploadProcessor;
import com.treasuredata.client.TDClient;
import com.treasuredata.client.model.TDBulkImportSession;

public final class BulkImportCommand extends BulkImport {
    private static final Logger LOG = Logger.getLogger(BulkImportCommand.class.getName());

    protected CommandHelper commandHelper;

    public BulkImportCommand(Properties props) {
        super(props);
        commandHelper = new CommandHelper();
    }

    public void doMain(final String[] args) throws Exception {
        try {
            if (args.length < 1) {
                throw new IllegalArgumentException("Command not specified");
            }

            String cmdName = args[0].toLowerCase();
            Configuration.Command cmd = Configuration.Command.fromString(cmdName);
            if (cmd == null) {
                throw new IllegalArgumentException(String.format("Command not support: %s", cmdName));
            }

            Properties props = System.getProperties();
            new BulkImportCommand(props).doCommand(cmd, args);
        } catch (IllegalArgumentException e) {
            String msg = String.format("Cannot execute your command: %s", e.getMessage());
            commandHelper.printErrorLine(msg);
            LOG.log(Level.SEVERE, msg, e);
            System.exit(2);
        }
    }

    public void doCommand(final Configuration.Command cmd, final String[] args) throws Exception {
        // dump td-import and td-client versions in the log file
        LOG.info(String.format("Use td-import-java: " + Configuration.getTDImportVersion()));
        LOG.info(String.format("Use td-client-java: " + Configuration.getTDClientVersion()));

        if (cmd.equals(Configuration.Command.PREPARE)) {
            doPrepareCommand(args);
        } else if (cmd.equals(Configuration.Command.UPLOAD)) {
            doUploadCommand(args);
        } else if (cmd.equals(Configuration.Command.AUTO)) {
            String[] realargs = new String[args.length + 3];
            realargs[args.length] = Configuration.BI_UPLOAD_AUTO_COMMIT_HYPHEN;
            realargs[args.length + 1] = Configuration.BI_UPLOAD_AUTO_PERFORM_HYPHEN;
            realargs[args.length + 2] = Configuration.BI_UPLOAD_AUTO_DELETE_HYPHEN;
            System.arraycopy(args, 0, realargs, 0, args.length);

            props.setProperty(Configuration.CMD_AUTO_ENABLE, "true");

            doUploadCommand(realargs);
        } else {
            throw new UnsupportedOperationException("Fatal error");
        }
    }

    public void doPrepareCommand(final String[] args) throws Exception {
        LOG.info(String.format("Start '%s' command", Configuration.CMD_PREPARE));

        // create configuration for 'prepare' processing
        PrepareConfiguration prepareConf = createPrepareConf(args);

        // extract and get source names from command-line arguments
        Source[] srcs = getSources(prepareConf, 1);
        if (srcs.length == 0) {
            throw new IllegalArgumentException(String.format(
                    "Cannot prepare with no content. Please check your command to ensure " +
                    prepareConf.getSourceTargetDescr() + " is provided."));
        }
        List<String> srcNames = new ArrayList<String>();
        for (Source src : srcs) {
            srcNames.add(src.getPath());
        }
        commandHelper.showPrepare(srcs, prepareConf.getOutputDirName());

        MultiThreadPrepareProcessor prepareProc =
                createAndStartPrepareProcessor(prepareConf);

        // create prepare tasks
        com.treasure_data.td_import.prepare.Task[] tasks =
                createPrepareTasks(prepareConf, srcs);

        // start elapsed time timer
        commandHelper.startPrepare();
        // creates the prepare tasks
        startPrepareTasks(prepareConf, tasks);

        // set *finish* tasks on task queue
        setPrepareFinishTasks(prepareConf);

        // wait for finishing prepare processing
        // extract task results of each prepare processing
        List<com.treasure_data.td_import.TaskResult<?>> prepareResults =
                stopPrepareProcessor(prepareProc);

        commandHelper.finishPrepare();
        commandHelper.showPrepareResults(prepareResults);
        commandHelper.listNextStepOfPrepareProc(prepareResults);

        LOG.info(String.format("Finished '%s' command", Configuration.CMD_PREPARE));

        // handle exit codes
        if (!hasNoPrepareError(prepareResults)) {
            throw new RuntimeException();
        }
    }

    public void doUploadCommand(final String[] args) throws Exception {
        LOG.info(String.format("Start '%s' command", Configuration.CMD_UPLOAD));

        // create configuration for 'upload' processing
        UploadConfiguration uploadConf = createUploadConf(args);

        // create TDClient objects
        TDClient tdClient = uploadConf.createTDClient();

        // configure session name
        TaskResult<?> e = null;
        String sessionName;
        int srcPos;
        if (uploadConf.autoCreate()) { // '--auto-create my_db.my_tbl' option
            // create session automatically
            sessionName = createBulkImportSessionName(uploadConf, tdClient);

            srcPos = 1;
        } else {
            // get session name from command-line arguments
            sessionName = getBulkImportSessionName(uploadConf);

            // validate that the session is live or not
            e = UploadProcessor.checkSession(tdClient, sessionName);
            if (e.error != null) {
                throw new IllegalArgumentException(e.error);
            }

            srcPos = 2;
        }

        TDBulkImportSession sess = UploadProcessor.showSession(tdClient, sessionName);
        if (sess == null) {
            throw new IllegalArgumentException(String.format(
                    "Bulk import session is not specified or created yet."));
        }
        // if session is already freezed, exception is thrown.
        if (sess.isUploadFrozen()) {
            throw new IllegalArgumentException(String.format(
                    "Bulk import session '%s' is already freezed. Please check it with 'td import:show %s'",
                    sessionName, sessionName));
        }

        // get and extract uploaded sources from command-line arguments
        Source[] srcs = getSources(uploadConf, srcPos);
        if (srcs.length == 0) {
            throw new IllegalArgumentException(String.format(
                    "Cannot upload empty content to bulk import session '%s'. " + 
                    "Please check your command to ensure "  +
                    uploadConf.getSourceTargetDescr() + " is provided. ",
                    sessionName));
        }
        List<String> srcNames = new ArrayList<String>();
        for (Source src : srcs) {
            srcNames.add(src.getPath());
        }
        commandHelper.showUpload(srcs, sessionName);

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
                commandHelper.startUpload();
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
                // prepare tasks.
                commandHelper.startPrepare();
                commandHelper.startUpload();
                startPrepareTasks(prepareConf, tasks);

                // set *finish* prepare tasks on prepare task queue.
                // after those prepare tasks are finished, automatically the
                // upload tasks are put on upload task queue.
                setPrepareFinishTasks(prepareConf);

                // wait for finishing all prepare tasks by using *finish* prepare tasks.
                results.addAll(stopPrepareProcessor(prepareProc));

                commandHelper.finishPrepare();
                commandHelper.showPrepareResults(results);
                commandHelper.listNextStepOfPrepareProc(results);

                hasNoPrepareError = hasNoPrepareError(results);
            }
        } finally {
            // put *finish* upload tasks on upload task queue
            setUploadFinishTasks(uploadConf);
        }

        // wait for finishing all upload tasks by using *finish* tasks.
        results.addAll(stopUploadProcessor(uploadProc));

        commandHelper.finishUpload();
        commandHelper.showUploadResults(results);
        commandHelper.listNextStepOfUploadProc(results, sessionName);

        if (!hasNoUploadError(results) || !hasNoPrepareError) {
            throw new RuntimeException();
        }

        // 'auto-perform' and 'auto-commit'
        results.add(UploadProcessor.processAfterUploading(tdClient, uploadConf, sessionName));

        // 'auto-delete'
        if (hasNoUploadError(results) && uploadConf.autoDelete() && uploadConf.autoCommit()) {
            results.add(UploadProcessor.deleteSession(tdClient, sessionName));
        }

        LOG.info(String.format("Finished '%s' command", Configuration.CMD_UPLOAD));
    }

    public static void main(final String[] args) throws Exception {
        try {
            new BulkImportCommand(System.getProperties()).doMain(args);
        } catch (Exception e) {
            String msg = String.format("Cannot execute your command: %s (%s)", 
                e.getMessage(), e.getClass().getName());
            LOG.log(Level.SEVERE, msg);
            System.err.println(msg);
            System.exit(1);
        }

        System.exit(0);
    }
}
