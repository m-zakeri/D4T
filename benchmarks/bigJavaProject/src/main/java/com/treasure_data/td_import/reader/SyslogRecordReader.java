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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.treasure_data.td_import.model.StringColumnValue;
import com.treasure_data.td_import.model.TimeColumnSampling;
import com.treasure_data.td_import.model.TimeColumnValue;
import com.treasure_data.td_import.prepare.PrepareConfiguration;
import com.treasure_data.td_import.prepare.PreparePartsException;
import com.treasure_data.td_import.prepare.Strftime;
import com.treasure_data.td_import.prepare.SyslogPrepareConfiguration;
import com.treasure_data.td_import.writer.RecordWriter;
import com.treasure_data.td_import.writer.MsgpackGZIPRecordWriter;

public class SyslogRecordReader extends RegexRecordReader<SyslogPrepareConfiguration> {

    private static final Logger LOG = Logger.getLogger(SyslogRecordReader.class.getName());

    public static final String syslogPatString =
            "^([^ ]* [^ ]* [^ ]*) ([^ ]*) ([a-zA-Z0-9_\\/\\.\\-]*)(?:\\([a-zA-Z0-9_\\/\\.\\-]*\\))(?:\\[([0-9]+)\\])?[^\\:]*\\: *(.*)$";

    public static class ExtFileWriter extends MsgpackGZIPRecordWriter {
        protected long currentYear;

        public ExtFileWriter(PrepareConfiguration conf) {
            super(conf);
            currentYear = getCurrentYear();
        }

        @Override
        public void write(TimeColumnValue filter, StringColumnValue v)
                throws PreparePartsException {
            long time = filter.getTimeFormat().getTime(v.getString());
            time += currentYear;
            write(time);
        }

        private long getCurrentYear() {
            try {
                SimpleDateFormat f = new SimpleDateFormat("yyyy");
                Calendar cal = Calendar.getInstance();
                Date d = f.parse("" + cal.get(Calendar.YEAR));
                return d.getTime() / 1000;
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected BufferedReader reader;
    protected Pattern syslogPat;

    protected String line;
    protected List<String> row = new ArrayList<String>();

    public SyslogRecordReader(SyslogPrepareConfiguration conf, RecordWriter writer)
            throws PreparePartsException {
        super(conf, writer);
    }

    @Override
    public void validateSampleRecords(TimeColumnSampling[] sampleColumnValues,
            int lineNum) throws PreparePartsException {
        // ignore
    }

    @Override
    public void setTimeColumnValue(TimeColumnSampling[] sampleColumnValues,
            int timeColumnIndex, int aliasTimeColumnIndex) {
        timeColumnValue = new TimeColumnValue(2, new Strftime("%b %d %H:%M:%S"));
    }
}
