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
import com.treasure_data.td_import.source.LocalFileSource;
import com.treasure_data.td_import.source.Source;
import com.treasure_data.td_import.source.SourceDesc;
import com.treasure_data.td_import.upload.MultiThreadUploadProcessor;
import com.treasure_data.td_import.upload.UploadConfigurationBase;

public abstract class Import {
    private static final Logger LOG = Logger.getLogger(Import.class.getName());

    protected Properties props;

    public Import(Properties props) {
        this.props = props;
    }

    @Deprecated
    protected String[] getFileNames(PrepareConfiguration conf, int filePos) {
        List<String> argList = conf.getNonOptionArguments();
        final String[] fileNames = new String[argList.size() - filePos];
        for (int i = 0; i < fileNames.length; i++) {
            fileNames[i] = argList.get(i + filePos);
        }
        return fileNames;
    }

    protected Source[] getSources(PrepareConfiguration conf, int srcPos) {
        List<String> argList = conf.getNonOptionArguments();
        int len = argList.size() - srcPos;
        List<Source> srcs = new ArrayList<Source>();
        for (int i = 0; i < len; i++) {
            srcs.addAll(getSources(conf, argList.get(i + srcPos)));
        }
        return srcs.toArray(new Source[0]);
    }

    protected List<Source> getSources(PrepareConfiguration conf, String srcName) {
        try {
            SourceDesc desc = SourceDesc.create(srcName);
            return Source.Factory.createSources(desc);
        } catch (Throwable t) {
            // TODO FIXME #MN this error handling is no good.
            LOG.log(Level.WARNING, "fallback and create source as LocalFileSource: " + srcName, t);
            List<Source> srcs = new ArrayList<Source>();
            srcs.add(new LocalFileSource(srcName));
            return srcs;
        }
    }

    protected MultiThreadPrepareProcessor createAndStartPrepareProcessor(
            PrepareConfiguration conf) {
        MultiThreadPrepareProcessor proc = new MultiThreadPrepareProcessor(conf);
        proc.registerWorkers();
        proc.startWorkers();
        return proc;
    }

    protected void startPrepareTasks(final PrepareConfiguration conf,
            final com.treasure_data.td_import.prepare.Task[] tasks) {
        for (int i = 0; i < tasks.length; i++) {
            try {
                MultiThreadPrepareProcessor.addTask(tasks[i]);
            } catch (Throwable t) {
                LOG.severe("Error occurred During 'addTask' method call");
                LOG.throwing("Main", "addTask", t);
            }
        }
    }

    protected void setPrepareFinishTasks(final PrepareConfiguration conf) {
        try {
            MultiThreadPrepareProcessor.addFinishTask(conf);
        } catch (Throwable t) {
            LOG.severe("Error occurred During 'addFinishTask' method call");
            LOG.throwing("Main", "addFinishTask", t);
        }
    }

    protected List<com.treasure_data.td_import.TaskResult<?>> stopPrepareProcessor(
            MultiThreadPrepareProcessor proc) {
        // wait for finishing prepare processing
        proc.joinWorkers();

        // wait for finishing prepare processing
        List<com.treasure_data.td_import.TaskResult<?>> results =
                new ArrayList<com.treasure_data.td_import.TaskResult<?>>();
        results.addAll(proc.getTaskResults());
        return results;
    }

    protected boolean hasNoPrepareError(List<com.treasure_data.td_import.TaskResult<?>> results) {
        boolean hasNoError = true;
        for (com.treasure_data.td_import.TaskResult<?> result : results) {
            if (! (result instanceof com.treasure_data.td_import.prepare.TaskResult)) {
                continue;
            }

            if (result.error != null) {
                //result.error.printStackTrace(); // for debug
                hasNoError = false;
                break;
            }
        }
        return hasNoError;
    }

    protected MultiThreadUploadProcessor createAndStartUploadProcessor(UploadConfigurationBase conf) {
        MultiThreadUploadProcessor proc = new MultiThreadUploadProcessor(conf);
        proc.registerWorkers();
        proc.startWorkers();
        return proc;
    }

    protected List<com.treasure_data.td_import.TaskResult<?>> stopUploadProcessor(
            MultiThreadUploadProcessor proc) {
        // wait for finishing upload processing
        proc.joinWorkers();

        // wait for finishing upload processing
        List<com.treasure_data.td_import.TaskResult<?>> results =
                new ArrayList<com.treasure_data.td_import.TaskResult<?>>();
        results.addAll(proc.getTaskResults());
        return results;
    }

    protected boolean hasNoUploadError(List<com.treasure_data.td_import.TaskResult<?>> results) {
        boolean hasNoError = true;
        for (com.treasure_data.td_import.TaskResult<?> result : results) {
            if (! (result instanceof com.treasure_data.td_import.upload.TaskResult)) {
                continue;
            }

            if (result.error != null) {
                hasNoError = false;
                break;
            }
        }
        return hasNoError;
    }

    protected void showHelp(Configuration.Command cmd, PrepareConfiguration conf, String[] args) {
        if (conf.hasHelpOption()) {
            System.out.println(cmd.showHelp(conf, props));
            System.exit(0);
        }
    }
}
