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
package com.treasure_data.td_import.writer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;

import com.treasure_data.td_import.prepare.PrepareConfiguration;
import com.treasure_data.td_import.prepare.PreparePartsException;
import com.treasure_data.td_import.prepare.Task;
import com.treasure_data.td_import.prepare.TaskResult;

public class JSONRecordWriter extends AbstractRecordWriter {

    private Map<String, Object> record;
    private List<Object> recordElements;

    public JSONRecordWriter(PrepareConfiguration conf) {
        super(conf);
    }

    @Override
    public void configure(Task task, TaskResult result) throws PreparePartsException {
        super.configure(task, result);
    }

    @Override
    public void writeBeginRow(int size) throws PreparePartsException {
        if (record != null || recordElements != null) {
            throw new IllegalStateException("record must be null");
        }
        record = new HashMap<String, Object>();
        recordElements = new ArrayList<Object>();
    }

    @Override
    public void write(String v) throws PreparePartsException {
        recordElements.add(v);
    }

    @Override
    public void write(int v) throws PreparePartsException {
        recordElements.add(v);
    }

    @Override
    public void write(long v) throws PreparePartsException {
        recordElements.add(v);
    }

    @Override
    public void write(double v) throws PreparePartsException {
        recordElements.add(v);
    }

    @Override
    public void write(List<Object> v) throws PreparePartsException {
        recordElements.add(v);
    }

    @Override
    public void write(Map<Object, Object> v) throws PreparePartsException {
        recordElements.add(v);
    }

    @Override
    public void writeNil() throws PreparePartsException {
        recordElements.add(null);
    }

    @Override
    public void writeEndRow() throws PreparePartsException {
        int size = recordElements.size() / 2;
        for (int i = 0; i < size; i++) {
            String key = (String) recordElements.get(2 * i);
            Object val = recordElements.get(2 * i + 1);
            record.put(key, val);
        }
    }

    public String toJSONString() {
        return JSONValue.toJSONString(getRecord());
    }

    private Map<String, Object> getRecord() {
        return record;
    }

    @Override
    public void close() throws IOException {
        if (record != null) {
            record.clear();
            record = null;
        }
        if (recordElements != null) {
            recordElements.clear();
            recordElements = null;
        }
    }

}
