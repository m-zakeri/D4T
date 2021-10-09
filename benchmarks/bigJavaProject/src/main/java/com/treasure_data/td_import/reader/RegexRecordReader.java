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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.treasure_data.td_import.model.TimeColumnSampling;
import com.treasure_data.td_import.prepare.PreparePartsException;
import com.treasure_data.td_import.prepare.RegexPrepareConfiguration;
import com.treasure_data.td_import.prepare.Task;
import com.treasure_data.td_import.writer.RecordWriter;
import com.treasure_data.td_import.writer.JSONRecordWriter;

public class RegexRecordReader<T extends RegexPrepareConfiguration> extends FixedColumnsRecordReader<T> {
    private static final Logger LOG = Logger.getLogger(RegexRecordReader.class.getName());

    protected BufferedReader reader;
    protected Pattern pat;

    protected String line;
    protected List<String> record = new ArrayList<String>();

    public RegexRecordReader(T conf, RecordWriter writer)
            throws PreparePartsException {
        super(conf, writer);
    }

    @Override
    public void configure(Task task) throws PreparePartsException {
        super.configure(task);

        sample(task);

        try {
            reader = new BufferedReader(new InputStreamReader(
                    task.createInputStream(conf.getCompressionType()),
                    conf.getCharsetDecoder()));
        } catch (IOException e) {
            throw new PreparePartsException(e);
        }

        pat = Pattern.compile(conf.getRegexPattern());
    }

    public void sample(Task task) throws PreparePartsException {
        BufferedReader sampleReader = null;

        try {
            sampleReader = new BufferedReader(new InputStreamReader(
                    task.createInputStream(conf.getCompressionType()),
                    conf.getCharsetDecoder()));

            // get index of 'time' column
            // [ "time", "name", "price" ] as all columns is given,
            // the index is zero.
            int timeColumnIndex = getTimeColumnIndex();

            // get index of specified alias time column
            // [ "timestamp", "name", "price" ] as all columns and
            // "timestamp" as alias time column are given, the index is zero.
            //
            // if 'time' column exists in row data, the specified alias
            // time column is ignore.
            int aliasTimeColumnIndex = getAliasTimeColumnIndex(timeColumnIndex);

            // if 'time' and the alias columns or 'primary-key' column don't exist, ...
            validateTimeAndPrimaryColumn(timeColumnIndex, aliasTimeColumnIndex);

            boolean isFirstRow = true;
            List<String> firstRow = new ArrayList<String>();
            final int sampleRowSize = conf.getSampleRowSize();
            TimeColumnSampling[] sampleColumnValues = new TimeColumnSampling[columnNames.length];
            for (int i = 0; i < sampleColumnValues.length; i++) {
                sampleColumnValues[i] = new TimeColumnSampling(sampleRowSize);
            }

            // read some records
            for (int i = 0; i < sampleRowSize; i++) {
                if (!isFirstRow && (columnTypes == null || columnTypes.length == 0)) {
                    break;
                }

                record.clear();
                line = sampleReader.readLine();

                if (line == null) {
                    break;
                }

                Pattern samplePat = Pattern.compile(conf.getRegexPattern());
                Matcher sampleMat = samplePat.matcher(line);

                if (!sampleMat.matches()) {
                    throw new PreparePartsException("Don't match");
                }

                for (int j = 1; j < (columnNames.length + 1); j++) { // extract groups
                    record.add(sampleMat.group(j));
                }

                if (isFirstRow) {
                    firstRow.addAll(record);
                    isFirstRow = false;
                }

                validateSampleRecords(sampleColumnValues, i);

                // sampling
                for (int j = 0; j < sampleColumnValues.length; j++) {
                    sampleColumnValues[j].parse(record.get(j));
                }
            }

            // initialize types of all columns
            initializeColumnTypes(sampleColumnValues);

            // initialize time column value
            setTimeColumnValue(sampleColumnValues, timeColumnIndex, aliasTimeColumnIndex);

            initializeWrittenRecord();

            // check properties of exclude/only columns
            setSkipColumns();

            record.clear();
            record.addAll(firstRow);

            // print first sample record
            printSample();
        } catch (IOException e) {
            throw new PreparePartsException(e);
        } finally {
            if (sampleReader != null) {
                try {
                    sampleReader.close();
                } catch (IOException e) {
                    throw new PreparePartsException(e);
                }
            }
        }
    }

    protected void validateSampleRecords(TimeColumnSampling[] sampleColumnValues, int lineNum)
            throws PreparePartsException {
        if (sampleColumnValues.length != record.size()) {
            throw new PreparePartsException(
                    String.format("The number of columns to be processed (%d) must " +
                                  "match the number of column types (%d): check that the " +
                                  "number of column types you have defined matches the " +
                                  "expected number of columns being read/written [line: %d] %s",
                            record.size(), columnTypes.length, lineNum, record));
        }
    }

    @Override
    public boolean readRecord() throws IOException, PreparePartsException {
        record.clear();
        if ((line = reader.readLine()) == null) {
            return false;
        }

        incrementLineNum();

        Matcher mat = pat.matcher(line);

        if (!mat.matches()) {
            writer.incrementErrorRowNum();
            throw new PreparePartsException(String.format(
                    "line is not matched at apache common log format [line: %d]",
                    getLineNum()));
        }

        // extract groups
        for (int i = 1; i < (columnNames.length + 1); i++) {
            record.add(mat.group(i));
        }

        validateRecords();

        return true;
    }

    private void validateRecords() throws PreparePartsException {
        if (record.size() != columnTypes.length) {
            throw new PreparePartsException(String.format(
                    "The number of columns to be processed (%d) must " +
                    "match the number of column types (%d): check that the " +
                    "number of column types you have defined matches the " +
                    "expected number of columns being read/written [line: %d]",
                    record.size(), columnTypes.length, getLineNum()));
        } 
    }

    @Override
    public void convertTypes() throws PreparePartsException {
        for (int i = 0; i < this.record.size(); i++) {
            columnTypes[i].convertType(this.record.get(i), writtenRecord.getValue(i));
        }
    }

    @Override
    public String getCurrentRecord() {
        return line;
    }

    @Override
    public void close() throws IOException {
        super.close();

        if (reader != null) {
            reader.close();
        }
    }
}
