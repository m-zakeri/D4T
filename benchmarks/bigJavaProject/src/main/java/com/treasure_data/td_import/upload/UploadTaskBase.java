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

import com.treasure_data.td_import.source.Source;

public class UploadTaskBase implements com.treasure_data.td_import.Task {
    protected static final String TAG = "__FINISH__";

    protected static final UploadTaskBase FINISH_TASK = new UploadTaskBase(TAG, 0);

    public String fileName;
    public long size;

    // unit testing
    public boolean isTest = false;
    public byte[] testBinary = null;

    public UploadTaskBase(Source source) {
        this(source.getPath(), source.getSize());
    }

    private UploadTaskBase(String fileName, long size) {
        this.fileName = fileName;
        this.size = size;
    }

    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException();
    }

    public boolean endTask() {
        return fileName.equals(TAG) && size == 0;
    }

    @Override
    public void startHook() {
        // do nothing
    }

    @Override
    public void finishHook(String outputFileName) {
        // do nothing
    }
}