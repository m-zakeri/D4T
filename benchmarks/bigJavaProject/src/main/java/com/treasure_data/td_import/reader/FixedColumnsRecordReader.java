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
package com.treasure_data.td_import.reader;

import java.io.IOException;
import java.util.logging.Logger;

import com.treasure_data.td_import.prepare.FixedColumnsPrepareConfiguration;
import com.treasure_data.td_import.prepare.PreparePartsException;
import com.treasure_data.td_import.prepare.Task;
import com.treasure_data.td_import.writer.JSONRecordWriter;
import com.treasure_data.td_import.writer.RecordWriter;

public abstract class FixedColumnsRecordReader<T extends FixedColumnsPrepareConfiguration> extends AbstractRecordReader<T> {

    private static final Logger LOG = Logger.getLogger(FixedColumnsRecordReader.class.getName());

    public FixedColumnsRecordReader(T conf, RecordWriter writer) throws PreparePartsException {
        super(conf, writer);
    }

    @Override
    public void configure(Task task) throws PreparePartsException {
        super.configure(task);
    }

    protected abstract void sample(Task task) throws PreparePartsException;

    protected void printSample() throws IOException, PreparePartsException {
        JSONRecordWriter w = null;
        String ret = null;
        try {
            w = new JSONRecordWriter(conf);
            w.setActualColumnNames(getActualColumnNames());
            w.setColumnNames(getColumnNames());
            w.setColumnTypes(getColumnTypes());
            w.setSkipColumns(getSkipColumns());
            w.setTimeColumnValue(getTimeColumnValue());

            // convert each column in row
            convertTypes();
            // write each column value
            w.next(writtenRecord);
            ret = w.toJSONString();
        } catch (PreparePartsException e) {
            // ignore
        } finally {
            if (w != null) {
                w.close();
            }
        }

        String msg = null;
        if (ret != null) {
            msg = "sample row: " + ret;
        } else  {
            msg = "cannot get sample row";
        }
        System.out.println(msg);
        LOG.info(msg);
    }

    public void readHeader() throws IOException, PreparePartsException {
        throw new UnsupportedOperationException("this method should be implemented in sub-classes");
    }

    @Override
    public boolean readRecord() throws IOException, PreparePartsException {
        throw new UnsupportedOperationException("this method should be implemented in sub-classes");
    }

    public void validateRecord(int expected, int actual) throws PreparePartsException {
        if (expected == actual) {
            return;
        }

        throw new PreparePartsException(String.format(
                "The number of columns to be processed (%d) must " +
                        "match the number of column types (%d): check that the " +
                        "number of column types you have defined matches the " +
                        "expected number of columns being read/written [line: %d]",
                        actual, expected, getLineNum()));
    }

    @Override
    public void convertTypes() throws PreparePartsException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

}