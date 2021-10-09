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

import com.treasuredata.client.TDClient;
import com.treasuredata.client.TDClientException;
import com.treasuredata.client.model.TDBulkImportSession;
import com.treasuredata.client.model.TDDatabase;
import com.treasuredata.client.model.TDJob;
import com.treasuredata.client.model.TDTable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UploadProcessor extends UploadProcessorBase {

    private static final Logger LOG = Logger.getLogger(UploadProcessor.class.getName());

    private static TDBulkImportSession summary;

    protected TDClient client;

    public UploadProcessor(TDClient client, UploadConfiguration conf) {
        super(conf);
        this.client = client;
    }

    public TaskResult execute(final UploadTaskBase base) {
        final UploadTask task = (UploadTask) base;
        TaskResult result = new TaskResult();
        result.task = task;

        if (task.size == 0) {
            String msg = String.format(
                    "Uploaded file is 0 bytes or not exist: %s", task.fileName);
            LOG.severe(msg);
            result.error = new IOException(msg);
            return result;
        }

        try {
            System.out.println(String.format("Uploading %s (%d bytes)...", task.fileName, task.size));
            LOG.info(String.format("Uploading %s (%d bytes) to session %s as part %s",
                    task.fileName, task.size, task.sessName, task.partName));

            long time = System.currentTimeMillis();
            executeUpload(task);
            time = System.currentTimeMillis() - time;
            task.finishHook(task.fileName);

            LOG.info(String.format(
                    "Uploaded file %s (%d bytes) to session %s as part %s (time: %d sec.)",
                    task.fileName, task.size, task.sessName, task.partName, (time / 1000)));
        } catch (TDClientException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
            result.error = new IOException(e);
        }
        return result;
    }

    protected void executeUpload(final UploadTask task) throws TDClientException {
        if (!task.isTest) {
            client.uploadBulkImportPart(task.sessName, task.partName, new File(task.fileName));
        } else {
            BufferedOutputStream outputStream = null;
            try {
                File tempFile = File.createTempFile("test", ".tmp");
                outputStream = new BufferedOutputStream(new FileOutputStream(tempFile));
                outputStream.write(task.testBinary);
                client.uploadBulkImportPart(task.sessName, task.partName, tempFile);
            } catch (Exception ex) {
                // ignorable since this situation only happens while testing
            }
            finally {
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (Exception ex) {
                    // ignorable since this situation only happens while testing
                }
            }
        }
    }

    protected InputStream createInputStream(final UploadTask task) throws IOException {
        if (!task.isTest) {
            return new BufferedInputStream(new FileInputStream(task.fileName));
        } else {
            return new ByteArrayInputStream(task.testBinary);
        }
    }

    public static TaskResult processAfterUploading(final TDClient client, UploadConfiguration conf, String sessName)
            throws UploadPartsException {
        TaskResult err = null;

        if (!conf.autoPerform()) {
            return new TaskResult();
        }

        // freeze
        err = freezeSession(client, sessName);
        if (err.error != null) {
            return err;
        }

        // perform
        err = performSession(client, sessName);
        if (err.error != null) {
            return err;
        }

        // check session status
        TDBulkImportSession summary = null;
        try {
            summary = showSession(client, sessName);

            StringBuilder sbuf = new StringBuilder();
            sbuf.append(String.format("Show status of bulk import session %s", summary.getName())).append("\n");
            sbuf.append("  Performing job ID : " + summary.getJobId()).append("\n");
            sbuf.append("  Name              : " + summary.getName()).append("\n");
            sbuf.append("  Status            : " + summary.getStatus()).append("\n");

            System.out.println(sbuf.toString());
            LOG.info(sbuf.toString());
        } catch (IOException e) {
            String m = String.format("Session status checking failed: %s", e.getMessage());
            System.out.println(m);
            LOG.severe(m);
            err.error = e;
        }

        if (summary == null) {
            return err;
        }

        // TODO FIXME #MN need log message

        if (!conf.autoCommit()) {
            return new TaskResult();
        }

        // wait performing
        err = waitPerform(client, sessName);
        if (err.error != null) {
            return err;
        }

        // check error of perform
        try {
            summary = showSession(client, sessName);

            StringBuilder sbuf = new StringBuilder();
            sbuf.append(String.format("Show the result of bulk import session %s", summary.getName())).append("\n");
            sbuf.append("  Performing job ID : " + summary.getJobId()).append("\n");
            sbuf.append("  Valid parts       : " + summary.getValidParts()).append("\n");
            sbuf.append("  Error parts       : " + summary.getErrorParts()).append("\n");
            sbuf.append("  Valid records     : " + summary.getValidRecords()).append("\n");
            sbuf.append("  Error records     : " + summary.getErrorRecords()).append("\n");

            System.out.println(sbuf.toString());
            LOG.info(sbuf.toString());
        } catch (IOException e) {
            String m = String.format("Error records checking failed: %s", e.getMessage());
            System.out.println(m);
            LOG.severe(m);
            err.error = e;
        }

        if (summary == null) {
            return err;
        }

        if (summary.getValidRecords() == 0) {
            String msg;
            if (summary.getErrorRecords() != 0) {
                msg = String.format(
                        "The td import command stopped because the perform job (%s) reported 0 valid records.\n"
                      + "Please execute the 'td import:error_records %s' command to check the invalid records.",
                      summary.getJobId(), summary.getName());
            } else { // both of valid records and error records are 0.
                msg = String.format(
                        "The td import command stopped because the perform job (%s) reported 0 valid records. Commit operation will be skipped.",
                        summary.getJobId());
            }

            System.out.println(msg);
            LOG.severe(msg);

            err.error = new UploadPartsException(msg);
            return err;
        } else if (summary.getErrorParts() != 0 || summary.getErrorRecords() != 0) {
            String msg = String.format(
                    "Perform job (%s) reported %d error parts and %d error records.\n"
                  + "If error records exist, td import command stops.\n"
                  + "If you want to check error records by the job, please execute command 'td import:error_records %s'.\n"
                  + "If you ignore error records and want to commit your performed data to your table, you manually can execute command 'td import:commit %s'.\n"
                  + "If you want to delete your bulk_import session, you also can execute command 'td import:delete %s'.",
                  summary.getJobId(), summary.getErrorParts(), summary.getErrorRecords(),
                  summary.getName(), summary.getName(), summary.getName());
            System.out.println(msg);
            LOG.severe(msg);

            err.error = new UploadPartsException(msg);
            return err;
        }

        // commit and wait commit
        err = commitAndWaitCommit(client, sessName);
        if (err.error != null) {
            return err;
        }

        return new TaskResult();
    }

    public static TDBulkImportSession showSession(final TDClient client, final String sessionName) throws IOException {
        LOG.fine(String.format("Show bulk import session %s", sessionName));

        try {
            return client.getBulkImportSession(sessionName);
        } catch (TDClientException e) {
            LOG.severe(e.getMessage());
            throw new IOException(e);
        }
    }

    public static TaskResult freezeSession(final TDClient client, final String sessionName) {
        String m = String.format("Freeze bulk import session %s", sessionName);
        System.out.println(m);
        LOG.info(m);

        TaskResult err = new TaskResult();
        try {
            client.freezeBulkImportSession(sessionName);
        } catch (TDClientException e) {
            m = String.format("Cannot freeze session %s, %s", sessionName, e.getMessage());
            System.out.println(m);
            LOG.severe(m);
            err.error = new IOException(e);
        }
        return err;
    }

    public static TaskResult performSession(final TDClient client, final String sessionName) {
        String m = String.format("Perform bulk import session %s", sessionName);
        System.out.println(m);
        LOG.info(m);

        TaskResult err = new TaskResult();
        try {
            client.performBulkImportSession(sessionName);
        } catch (TDClientException e) {
            m = String.format("Cannot perform bulk import session %s, %s", sessionName, e.getMessage());
            System.out.println(m);
            LOG.severe(m);
            err.error = new IOException(e);
        }
        return err;
    }

    public static TaskResult waitPerform(final TDClient client, final String sessionName) {
        String m = String.format("Wait %s bulk import session performing...", sessionName);
        System.out.println(m);
        LOG.info(m);

        TaskResult err = new TaskResult();
        long waitTime = System.currentTimeMillis();
        while (true) {
            try {
                summary = client.getBulkImportSession(sessionName);

                if (summary.getStatus().equals(TDBulkImportSession.ImportStatus.READY)){
                    break;
                } else if (summary.getStatus().equals(TDBulkImportSession.ImportStatus.UPLOADING)) {
                    throw new IOException("performing failed");
                }

                String jobId = summary.getJobId();
                TDJob.Status jobStatus = client.jobStatus(jobId).getStatus();
                if (!(jobStatus.equals(TDJob.Status.BOOTING) ||
                        jobStatus.equals(TDJob.Status.QUEUED) ||
                        jobStatus.equals(TDJob.Status.RUNNING) ||
                        jobStatus.equals(TDJob.Status.SUCCESS))) {
                    throw new IOException("performing failed: the job status was changed to "
                            + jobStatus.toString());
                }

                try {
                    long deltaTime = System.currentTimeMillis() - waitTime;
                    LOG.fine(String.format("Waiting for about %d sec.", (deltaTime / 1000)));
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    // ignore
                }
            } catch (TDClientException e) {
                m = String.format("Give up waiting %s bulk import session performing, %s", sessionName, e.getMessage());
                System.out.println(m);
                LOG.severe(m);
                err.error = new IOException(e);
                break;
            } catch (IOException e) {
                m = String.format("Give up waiting %s bulk import session performing, %s", sessionName, e.getMessage());
                System.out.println(m);
                LOG.severe(m);
                err.error = e;
                break;
            }
        }

        return err;
    }

    public static TaskResult commitAndWaitCommit(final TDClient client, final String sessionName) throws UploadPartsException {
        TaskResult err = new TaskResult();
        boolean firstRequest = true;
        int retryCount = 0;

        while (true) {
            if (!firstRequest) {
                if (retryCount > 8) {
                    return err;
                }

                try {
                    summary = client.getBulkImportSession(sessionName);
                } catch (TDClientException e) {
                    LOG.severe(e.getMessage());
                    err.error = new IOException(e);
                }

                if (summary.getStatus().equals(TDBulkImportSession.ImportStatus.COMMITTED)) {
                    return err;
                } else {
                    retryCount++;
                }
            }

            // commit
            err = commitSession(client, sessionName);
            firstRequest = false;
            if (err.error != null) {
                return err;
            }

            // wait commit
            err = waitCommit(client, sessionName);
        }
    }

    public static TaskResult commitSession(final TDClient client, final String sessionName) {
        String msg = String.format("Commit %s bulk import session", sessionName);
        System.out.println(msg);
        LOG.info(msg);

        TaskResult err = new TaskResult();
        try {
            client.commitBulkImportSession(sessionName);
        } catch (TDClientException e) {
            String emsg = String.format("Cannot commit '%s' bulk import session, %s", sessionName, e.getMessage());
            System.out.println(emsg);
            LOG.severe(emsg);
            err.error = new IOException(e);
        }
        return err;
    }

    public static TaskResult waitCommit(final TDClient client, final String sessionName) {
        String m = String.format("Wait %s bulk import session committing...", sessionName);
        System.out.println(m);
        LOG.info(m);

        TaskResult err = new TaskResult();
        long waitTime = System.currentTimeMillis();
        while (true) {
            try {
                summary = client.getBulkImportSession(sessionName);

                if (summary.getStatus().equals(TDBulkImportSession.ImportStatus.COMMITTED)){
                    break;
                } else if (summary.getStatus().equals(TDBulkImportSession.ImportStatus.READY)) {
                    throw new IOException("committing failed");
                }

                try {
                    long deltaTime = System.currentTimeMillis() - waitTime;
                    LOG.fine(String.format("Waiting for about %d sec.", (deltaTime / 1000)));
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    // ignore
                }
            } catch (TDClientException e) {
                m = String.format("Give up waiting %s bulk import session committing, %s", sessionName, e.getMessage());
                System.out.println(m);
                LOG.severe(m);
                err.error = new IOException(e);
                break;
            } catch (IOException e) {
                m = String.format("Give up waiting %s bulk import session committing, %s", sessionName, e.getMessage());
                System.out.println(m);
                LOG.severe(m);
                err.error = e;
                break;
            }
        }

        return err;
    }

    public static TaskResult checkDatabase(final TDClient client, final String databaseName) {
        LOG.info(String.format("Check database %s", databaseName));

        TaskResult err = new TaskResult();
        try {
            List<TDDatabase> dbs = client.listDatabases();
            boolean exist = false;
            for (TDDatabase db : dbs) {
                if (db.getName().equals(databaseName)) {
                    exist = true;
                    break;
                }
            }

            if (!exist) {
                throw new IOException(String.format("Not found database %s",
                        databaseName));
            }
        } catch (TDClientException e) {
            String msg = String.format(
                    "Cannot access database %s, %s. " +
                            "Please check it with 'td database:list'. " +
                            "If it doesn't exist, please create it with 'td database:create %s'.",
                    databaseName, e.getMessage(), databaseName);
            System.out.println(msg);
            LOG.severe(msg);
            err.error = new IOException(e);
        } catch (IOException e) {
            String msg = String.format(
                    "Cannot access database %s, %s. " +
                    "Please check it with 'td database:list'. " +
                    "If it doesn't exist, please create it with 'td database:create %s'.",
                    databaseName, e.getMessage(), databaseName);
            System.out.println(msg);
            LOG.severe(msg);
            err.error = e;
        }

        return err;
    }

    public static TaskResult checkTable(final TDClient client, final String databaseName, final String tableName) {
        LOG.info(String.format("Check table %s", tableName));

        TaskResult err = new TaskResult();
        try {
            List<TDTable> tbls = client.listTables(databaseName);
            boolean exist = false;
            for (TDTable tbl : tbls) {
                if (tbl.getName().equals(tableName)) {
                    exist = true;
                    break;
                }
            }

            if (!exist) {
                throw new IOException(String.format("Not found table %s", tableName));
            }
        } catch (TDClientException e) {
            String msg = String.format(
                    "Cannot access table '%s', %s. " +
                            "Please check it with 'td table:list %s'. " +
                            "If it doesn't exist, please create it with 'td table:create %s %s'.",
                    tableName, e.getMessage(), databaseName, databaseName, tableName);
            System.out.println(msg);
            LOG.severe(msg);
            err.error = new IOException(e);
        } catch (IOException e) {
            String msg = String.format(
                    "Cannot access table '%s', %s. " +
                    "Please check it with 'td table:list %s'. " +
                    "If it doesn't exist, please create it with 'td table:create %s %s'.",
                    tableName, e.getMessage(), databaseName, databaseName, tableName);
            System.out.println(msg);
            LOG.severe(msg);
            err.error = e;
        }
        return err;
    }

    public static TaskResult createSession(final TDClient client, final String sessionName,
                                           final String databaseName, final String tableName) {
        String msg = String.format("Create %s bulk_import session", sessionName);
        System.out.println(msg);
        LOG.info(msg);

        TaskResult err = new TaskResult();
        try {
            client.createBulkImportSession(sessionName, databaseName, tableName);
        }
        catch (TDClientException e) {
            String emsg = String.format(
                    "Cannot create bulk_import session %s by using %s:%s, %s. ",
                    sessionName, databaseName, tableName, e.getMessage());
            System.out.println(emsg);
            LOG.severe(emsg);
            err.error = new IOException(e);
        }
        return err;
    }

    public static TaskResult checkSession(final TDClient client, final String sessionName) {
        LOG.info(String.format("Check bulk_import session %s", sessionName));

        TaskResult err = new TaskResult();
        try {
            client.getBulkImportSession(sessionName);
        } catch (TDClientException e) {
            String msg = String.format(
                    "Cannot access bulk_import session %s, %s. " +
                    "Please check it with 'td bulk_import:list'. " +
                    "If it doesn't exist, please create it.",
                    sessionName, e.getMessage());
            System.out.println(msg);
            LOG.severe(msg);
            err.error = new IOException(e);
        }
        return err;
    }

    public static TaskResult deleteSession(final TDClient client, final String sessionName) {
        String msg = String.format("Delete bulk_import session %s", sessionName);
        System.out.println(msg);
        LOG.info(msg);

        TaskResult err = new TaskResult();
        try {
            client.deleteBulkImportSession(sessionName);
        } catch (TDClientException e) {
            String emsg = String.format(
                    "Cannot delete bulk_import session %s, %s. " +
                    "Please check it with 'td bulk_import:list'.",
                    sessionName, e.getMessage());
            System.out.println(emsg);
            LOG.severe(emsg);
            err.error = new IOException(e);
        }
        return err;
    }

}
