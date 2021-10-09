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

import com.treasure_data.td_import.source.LocalFileSource;
import com.treasure_data.td_import.source.Source;

public class SequentialImportTask extends SequentialUploadTaskBase {
    public String databaseName;
    public String tableName;

    public SequentialImportTask(String databaseName, String tableName, Source source) {
        super(source);
        this.databaseName = databaseName;
        this.tableName = tableName;
    }

    public com.treasure_data.td_import.upload.UploadTaskBase createNextTask(String outputFileName) {
        return new com.treasure_data.td_import.upload.ImportTask(
                databaseName, tableName, new LocalFileSource(outputFileName));
    }

    @Override
    public void finishHook(String outputFileName) {
        super.finishHook(outputFileName);
    }

    @Override
    public String toString() {
        return String.format("prepare_import_task{src=%s, database=%s, table=%s}",
                source, databaseName, tableName);
    }
}