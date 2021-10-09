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

public class TaskResult extends com.treasure_data.td_import.TaskResult<Task> {
    public long readLines = 0;
    public long convertedRows = 0;
    public long invalidRows = 0;

    public List<String> outFileNames = new ArrayList<String>();
    public List<Long> outFileSizes = new ArrayList<Long>();

    @Override
    public String toString() {
        return String.format(
                "prepare_task_result{task=%s, readLines=%d, convertedRows=%d, invalidRows=%d}",
                task, readLines, convertedRows, invalidRows);
    }
}