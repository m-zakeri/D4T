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

import com.treasure_data.td_import.prepare.Task;
import com.treasure_data.td_import.source.Source;
import com.treasure_data.td_import.upload.MultiThreadUploadProcessor;

public abstract class SequentialUploadTaskBase extends Task {
    public SequentialUploadTaskBase(Source source) {
        super(source);
    }

    @Override
    public void finishHook(String outputFileName) {
        super.finishHook(outputFileName);
        MultiThreadUploadProcessor.addTask(createNextTask(outputFileName));
    }

    public abstract com.treasure_data.td_import.upload.UploadTaskBase createNextTask(String outputFileName);

    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }
}