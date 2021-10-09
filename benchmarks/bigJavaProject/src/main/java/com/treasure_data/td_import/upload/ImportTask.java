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

public class ImportTask extends UploadTaskBase {
    public String databaseName;
    public String tableName;

    // unit testing
    public boolean isTest = false;
    public byte[] testBinary = null;

    public ImportTask(String databaseName, String tableName, Source source) {
        super(source);
        this.databaseName = databaseName;
        this.tableName = tableName;
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof ImportTask)) {
            return false;
        }

        ImportTask t = (ImportTask) obj;
        return t.databaseName.equals(databaseName) && t.tableName.equals(tableName);
    }
}