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

import com.treasure_data.td_import.source.Source;

public class CommandHelper {
    private long prepareStartTime;
    private long prepareElapsedTime;

    private long uploadStartTime;
    private long uploadElapsedTime;

    public CommandHelper() {
    }

    public void printLine(String message) {
        System.out.printf(message + "\n");
    }
    
    public void printErrorLine(String message) {
        System.err.printf(message + "\n");
    }

    public void printLine(String format, Object ... args) {
        System.out.printf(format + "\n", args);
    }

    public void startPrepare() {
        this.prepareStartTime = System.currentTimeMillis();
    }

    public void finishPrepare() {
        this.prepareElapsedTime = System.currentTimeMillis() - prepareStartTime;
    }

    public void startUpload() {
        this.uploadStartTime = System.currentTimeMillis();
    }

    public void finishUpload() {
        this.uploadElapsedTime = System.currentTimeMillis() - uploadStartTime;
    }

    public void showPrepare(Source[] sources, String outputDirName) {
        System.out.println();
        System.out.println("Preparing sources");
        System.out.println(String.format("  Output dir : %s", outputDirName));
        showSources(sources);
        System.out.println();
    }

    public void showUpload(Source[] sources, String sessionName) {
        System.out.println();
        System.out.println("Uploading prepared sources");
        System.out.println(String.format("  Session    : %s", sessionName));
        showSources(sources);
        System.out.println();
    }

    protected void showSources(Source[] sources) {
        for (Source source : sources) {
            String name = source.getPath();
            long size = source.getSize();
            System.out.println(String.format("  Source     : %s (%d bytes)", name, size));
        }
    }

    public void showPrepareResults(List<com.treasure_data.td_import.TaskResult<?>> results) {
        System.out.println();
        System.out.println("Prepare status:");
        System.out.println("  Elapsed time: " + (prepareElapsedTime / 1000) + " sec.");
        for (com.treasure_data.td_import.TaskResult<?> r : results) {
            if (! (r instanceof com.treasure_data.td_import.prepare.TaskResult)) {
                continue;
            }

            com.treasure_data.td_import.prepare.TaskResult result =
                    (com.treasure_data.td_import.prepare.TaskResult) r;
            String status = result.error == null ? Configuration.STAT_SUCCESS : Configuration.STAT_ERROR;
            System.out.println(String.format("  Source     : %s", result.task.getSource().getPath()));
            System.out.println(String.format("    Status          : %s", status));
            System.out.println(String.format("    Read lines      : %d", result.readLines));
            System.out.println(String.format("    Valid rows      : %d", result.convertedRows));
            System.out.println(String.format("    Invalid rows    : %d", result.invalidRows));
            int len = result.outFileNames.size();
            boolean first = true;
            for (int i = 0; i < len; i++) {
                if (first) {
                    System.out.println(String.format("    Converted Files : %s (%d bytes)",
                            result.outFileNames.get(i), result.outFileSizes.get(i)));
                    first = false;
                } else {
                    System.out.println(String.format("                      %s (%d bytes)",
                            result.outFileNames.get(i), result.outFileSizes.get(i)));
                }
            }
        }
        System.out.println();
    }

    public void listNextStepOfPrepareProc(List<com.treasure_data.td_import.TaskResult<?>> results) {
        System.out.println();
        System.out.println("Next steps:");

        List<String> readyToUploadFiles = new ArrayList<String>();

        for (com.treasure_data.td_import.TaskResult<?> r : results) {
            if (! (r instanceof com.treasure_data.td_import.prepare.TaskResult)) {
                continue;
            }

            com.treasure_data.td_import.prepare.TaskResult result =
                    (com.treasure_data.td_import.prepare.TaskResult) r;
            if (result.error == null) {
                int len = result.outFileNames.size();
                // success
                for (int i = 0; i < len; i++) {
                    readyToUploadFiles.add(result.outFileNames.get(i));
                }
            } else {
                // error
                System.out.println(String.format(
                        "  => check td-bulk-import.log and original %s: %s.",
                        result.task.getSource().getPath(), result.error.getMessage()));
            }
        }

        if(!readyToUploadFiles.isEmpty()) {
            System.out.println(String.format(
                        "  => execute following 'td import:upload' command. "
                        + "if the bulk import session is not created yet, please create it "
                        + "with 'td import:create <session> <database> <table>' command."));
            StringBuilder sb = new StringBuilder();
            sb.append("     $ td import:upload <session>");
            for(String file : readyToUploadFiles) {
                sb.append(" '");
                sb.append(file);
                sb.append("'");
            }
            System.out.println(sb);
        }
        System.out.println();
    }

    public void showUploadResults(List<com.treasure_data.td_import.TaskResult<?>> results) {
        System.out.println();
        System.out.println("Upload status:");
        System.out.println("  Elapsed time: " + (uploadElapsedTime / 1000) + " sec.");
        for (com.treasure_data.td_import.TaskResult<?> r : results) {
            if (! (r instanceof com.treasure_data.td_import.upload.TaskResult)) {
                continue;
            }

            com.treasure_data.td_import.upload.TaskResult result =
                    (com.treasure_data.td_import.upload.TaskResult) r;
            String status = result.error == null ? Configuration.STAT_SUCCESS : Configuration.STAT_ERROR;
            com.treasure_data.td_import.upload.UploadTask task =
                    (com.treasure_data.td_import.upload.UploadTask) result.task;
            System.out.println(String.format("  Source  : %s", result.task.fileName));
            System.out.println(String.format("    Status          : %s", status));
            System.out.println(String.format("    Part name       : %s", task.partName));
            System.out.println(String.format("    Size            : %d", task.size));
            System.out.println(String.format("    Retry count     : %d", result.retryCount));
        }
        System.out.println();
    }

    public void listNextStepOfUploadProc(List<com.treasure_data.td_import.TaskResult<?>> results,
            String sessionName) {
        System.out.println();
        System.out.println("Next Steps:");
        boolean hasErrors = false;
        int countUploadTaskResults = 0;
        for (com.treasure_data.td_import.TaskResult<?> r : results) {
            if (! (r instanceof com.treasure_data.td_import.upload.TaskResult)) {
                continue;
            }

            com.treasure_data.td_import.upload.TaskResult result =
                    (com.treasure_data.td_import.upload.TaskResult) r;
            countUploadTaskResults++;
            if (result.error != null) {
                // error
                System.out.println(String.format(
                        "  => check td-bulk-import.log and re-upload %s: %s.",
                        result.task.fileName, result.error.getMessage()));
                hasErrors = true;
            }
        }

        if (!hasErrors && countUploadTaskResults != 0) {
            // success
            System.out.println(String.format(
                    "  => execute 'td import:perform %s'.",
                    sessionName));
        }

        System.out.println();
    }

}
