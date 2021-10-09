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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class MultiThreadUploadProcessor {
    static class Worker extends Thread {
        private MultiThreadUploadProcessor parent;
        private UploadProcessorBase proc;
        AtomicBoolean isFinished = new AtomicBoolean(false);

        public Worker(MultiThreadUploadProcessor parent, UploadProcessorBase proc) {
            this.parent = parent;
            this.proc = proc;
        }

        @Override
        public void run() {
            while (true) {
                UploadTaskBase t = parent.taskQueue.poll();
                if (t == null) {
                    continue;
                } else if (t.endTask()) {
                    break;
                }

                TaskResult result = proc.execute(t);
                parent.setTaskResult(result);
            }
            isFinished.set(true);
        }
    }

    private static final Logger LOG = Logger.getLogger(MultiThreadUploadProcessor.class.getName());
    private static BlockingQueue<UploadTaskBase> taskQueue;

    static {
        taskQueue = new LinkedBlockingQueue<UploadTaskBase>();
    }

    public static synchronized void addTask(UploadTaskBase task) {
        taskQueue.add(task);
    }

    public static synchronized void clearTasks() {
        // the method is for tests
        taskQueue.clear();
    }

    public static synchronized void addFinishTask(UploadConfigurationBase conf) {
        for (int i = 0; i < conf.getNumOfUploadThreads() * 2; i++) {
            taskQueue.add(UploadTaskBase.FINISH_TASK);
        }
    }

    private UploadConfigurationBase conf;
    private List<Worker> workers;
    private List<TaskResult> results;

    public MultiThreadUploadProcessor(UploadConfigurationBase conf) {
        this.conf = conf;
        workers = new ArrayList<Worker>();
        results = new ArrayList<TaskResult>();
    }

    protected synchronized void setTaskResult(TaskResult error) {
        results.add(error);
    }

    public List<TaskResult> getTaskResults() {
        return results;
    }

    public void registerWorkers() {
        for (int i = 0; i < conf.getNumOfUploadThreads(); i++) {
            addWorker(createWorker(conf));
        }
    }

    protected Worker createWorker(UploadConfigurationBase conf) {
        return new Worker(this, createUploadProcessor(conf));
    }

    protected void addWorker(Worker w) {
        workers.add(w);
    }

    protected UploadProcessorBase createUploadProcessor(UploadConfigurationBase conf) {
        return conf.createNewUploadProcessor();
    }

    public void startWorkers() {
        for (int i = 0; i < workers.size(); i++) {
            workers.get(i).start();
        }
    }

    public void joinWorkers() {
        long waitSec = 1 * 1000;
        while (!workers.isEmpty()) {
            Worker last = workers.get(workers.size() - 1);
            if (last.isFinished.get()) {
                workers.remove(workers.size() - 1);
            }

            try {
                Thread.sleep(waitSec);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
}
