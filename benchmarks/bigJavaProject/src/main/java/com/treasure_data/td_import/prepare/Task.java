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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.treasure_data.td_import.source.LocalFileSource;
import com.treasure_data.td_import.source.Source;

public class Task implements com.treasure_data.td_import.Task {
    private static final Source FINISH_SRC = new LocalFileSource("__PREPARE_FINISH__");
    static final Task FINISH_TASK = new Task(FINISH_SRC);

    protected Source source;

    // unit testing
    public boolean isTest = false;
    public byte[] testBinary = null;

    public Task(Source source) {
        this.source = source;
    }

    public Source getSource() {
        return source;
    }

    public InputStream createInputStream(
            PrepareConfiguration.CompressionType compressionType)
            throws IOException {
        if (!isTest) {
            return compressionType.createInputStream(source.getInputStream());
        } else {
            return new ByteArrayInputStream(testBinary);
        }
    }

    @Override
    public void startHook() {
        // do nothing
    }

    @Override
    public void finishHook(String outputFileName) {
        // do nothing
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof Task)) {
            return false;
        }

        Task t = (Task) obj;
        return t.source.equals(source);
    }

    @Override
    public String toString() {
        return String.format("prepare_task{src=%s}", source);
    }

    @Override
    public boolean endTask() {
        return equals(FINISH_TASK);
    }
}