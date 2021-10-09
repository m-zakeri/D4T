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

import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;

import com.treasure_data.td_import.Options;
import com.treasure_data.td_import.model.AbstractColumnType;
import com.treasure_data.td_import.model.AbstractColumnValue;
import com.treasure_data.td_import.model.ColumnType;
import com.treasure_data.td_import.model.ColumnValue;
import com.treasure_data.td_import.model.TimeColumnValue;
import com.treasure_data.td_import.writer.RecordWriter;
import com.treasure_data.td_import.writer.MySQLTimestampAdaptor;

public class MySQLPrepareConfiguration extends PrepareConfiguration {

    public static class TimestampColumnType extends AbstractColumnType {

        public TimestampColumnType() {
            super("timestamp", 50);
        }

        @Override
        public ColumnValue createColumnValue(PrepareConfiguration config, int index) {
            return new TimestampColumnValue(config, index, this);
        }

        @Override
        public void convertType(String v, ColumnValue into)
                throws PreparePartsException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setColumnValue(Object v, ColumnValue cv)
                throws PreparePartsException {
            cv.set(v);
        }

        @Override
        public void filterAndWrite(ColumnValue v, TimeColumnValue filter,
                RecordWriter with) throws PreparePartsException {
            ((MySQLTimestampAdaptor) with).write(filter, (TimestampColumnValue) v);
        }

        @Override
        public void filterAndValidate(ColumnValue v, TimeColumnValue filter,
                RecordWriter with) throws PreparePartsException {
            ((MySQLTimestampAdaptor) with).validate(filter, (TimestampColumnValue) v);
        }
    }

    public static class TimestampColumnValue extends AbstractColumnValue {
        private long v;

        public TimestampColumnValue(PrepareConfiguration config, int index, ColumnType columnType) {
            super(config, index, columnType);
        }

        public void set(Object v) throws PreparePartsException {
            if (v == null) {
                this.v = 0L;
            } else {
                this.v = ((Timestamp) v).getTime() / 1000;
            }
        }

        public long getLong() {
            return v;
        }

        @Override
        public void parse(String v) throws PreparePartsException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(RecordWriter with) throws PreparePartsException {
            with.write(v);
        }
    }

    public static class DateColumnType extends AbstractColumnType {

        public DateColumnType() {
            super("date", 51);
        }

        @Override
        public ColumnValue createColumnValue(PrepareConfiguration config, int index) {
            return new DateColumnValue(config, index, this);
        }

        @Override
        public void convertType(String v, ColumnValue into)
                throws PreparePartsException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setColumnValue(Object v, ColumnValue cv)
                throws PreparePartsException {
            cv.set(v);
        }

        @Override
        public void filterAndWrite(ColumnValue v, TimeColumnValue filter,
                RecordWriter with) throws PreparePartsException {
            ((MySQLTimestampAdaptor) with).write(filter, (DateColumnValue) v);
        }

        @Override
        public void filterAndValidate(ColumnValue v, TimeColumnValue filter,
                RecordWriter with) throws PreparePartsException {
            ((MySQLTimestampAdaptor) with).validate(filter, (DateColumnValue) v);
        }
    }

    public static class DateColumnValue extends AbstractColumnValue {
        private long v;

        public DateColumnValue(PrepareConfiguration config, int index, ColumnType columnType) {
            super(config, index, columnType);
        }

        public void set(Object v) throws PreparePartsException {
            if (v == null) {
                this.v = 0L;
            } else {
                this.v = ((java.sql.Date) v).getTime() / 1000;
            }
        }

        public long getLong() {
            return v;
        }

        @Override
        public void parse(String v) throws PreparePartsException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(RecordWriter with) throws PreparePartsException {
            with.write(v);
        }
    }

    protected String jdbcUrl;
    protected String user;
    protected String password;
    protected String table;

    @Override
    public void configure(Properties props, Options options) {
        super.configure(props, options);

        setJdbcUrl();
        setUser();
        setPassword();
    }

    public void setJdbcUrl() {
        if (!optionSet.has(BI_PREPARE_PARTS_JDBC_CONNECTION_URL)) {
            throw new IllegalArgumentException("Not specified connection URL");
        }
        jdbcUrl = (String) optionSet.valueOf(BI_PREPARE_PARTS_JDBC_CONNECTION_URL);
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setUser() {
        if (!optionSet.has(BI_PREPARE_PARTS_JDBC_USER)) {
            throw new IllegalArgumentException("Not specified user");
        }
        user = (String) optionSet.valueOf(BI_PREPARE_PARTS_JDBC_USER);
    }

    public String getUser() {
        return user;
    }

    public void setPassword() {
        if (optionSet.has(BI_PREPARE_PARTS_JDBC_PASSWORD)) {
            password = (String) optionSet.valueOf(BI_PREPARE_PARTS_JDBC_PASSWORD);
        }
    }

    public String getPassword() {
        return password;
    }

    @Override
    public List<String> getNonOptionArguments() {
        List<String> argList = super.getNonOptionArguments();
        if (argList.size() > 2) {
            throw new IllegalArgumentException("Must not specified more than one table name at a time");
        }
        return argList;
    }
}
