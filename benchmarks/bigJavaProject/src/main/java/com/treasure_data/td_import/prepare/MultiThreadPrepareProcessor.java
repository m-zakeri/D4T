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
package com.treasure_data.td_import.prepare;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class MultiThreadPrepareProcessor {
    public static class Worker extends Thread {
        private MultiThreadPrepareProcessor parent;
        private PrepareProcessor proc;
        AtomicBoolean isFinished = new AtomicBoolean(false);

        public Worker(MultiThreadPrepareProcessor parent, PrepareProcessor proc) {
            this.parent = parent;
            this.proc = proc;
        }

        @Override
        public void run() {
            while (true) {
                Task t = parent.taskQueue.poll();
                if (t == null) {
                    continue;
                } else if (t.endTask()) {
                    break;
                }

                TaskResult result = proc.execute(t);
                parent.setResult(result);
            }
            isFinished.set(true);
        }
    }

    private static final Logger LOG = Logger.getLogger(MultiThreadPrepareProcessor.class.getName());
    private static BlockingQueue<Task> taskQueue;

    static {
        taskQueue = new LinkedBlockingQueue<Task>();
    }

    public static synchronized void addTask(Task task) {
        taskQueue.add(task);
    }

    public static synchronized void addFinishTask(PrepareConfiguration conf) {
        for (int i = 0; i < conf.getNumOfPrepareThreads(); i++) {
            taskQueue.add(Task.FINISH_TASK);
        }
    }

    private PrepareConfiguration conf;
    private List<Worker> workers;
    private List<TaskResult> results;

    public MultiThreadPrepareProcessor(PrepareConfiguration conf) {
        this.conf = conf;
        workers = new ArrayList<Worker>();
        results = new ArrayList<TaskResult>();
    }

    protected synchronized void setResult(TaskResult result) {
        results.add(result);
    }

    public List<TaskResult> getTaskResults() {
        return results;
    }

    public void registerWorkers() {
        for (int i = 0; i < conf.getNumOfPrepareThreads(); i++) {
            addWorker(createWorker(conf));
        }
    }

    public void registerWorkers(Worker[] ws) {
        for (Worker w : ws) {
            workers.add(w);
        }
    }

    protected Worker createWorker(PrepareConfiguration conf) {
        return new Worker(this, createPrepareProcessor(conf));
    }

    protected void addWorker(Worker w) {
        workers.add(w);
    }

    protected PrepareProcessor createPrepareProcessor(PrepareConfiguration conf) {
        return new PrepareProcessor(conf);
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