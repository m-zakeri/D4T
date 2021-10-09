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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import joptsimple.OptionSet;

import com.treasure_data.td_import.Options;
import com.treasure_data.td_import.Configuration;
import com.treasure_data.td_import.model.ColumnType;
import com.treasure_data.td_import.model.TimeValueTimeColumnValue;
import com.treasure_data.td_import.reader.ApacheRecordReader;
import com.treasure_data.td_import.reader.CSVRecordReader;
import com.treasure_data.td_import.reader.RecordReader;
import com.treasure_data.td_import.reader.JSONRecordReader;
import com.treasure_data.td_import.reader.MessagePackRecordReader;
import com.treasure_data.td_import.reader.MySQLTableReader;
import com.treasure_data.td_import.reader.RegexRecordReader;
import com.treasure_data.td_import.reader.SyslogRecordReader;
import com.treasure_data.td_import.source.Source;
import com.treasure_data.td_import.writer.RecordWriter;
import com.treasure_data.td_import.writer.MsgpackGZIPRecordWriter;
import com.treasure_data.td_import.writer.MySQLTimestampAdaptedMsgpackGZIPRecordWriter;

public class PrepareConfiguration extends Configuration {

    public static class Factory {
        protected Options options;

        public Factory(Properties props, boolean isUploaded) {
            options = new Options();
            if (isUploaded) {
                options.initUploadOptionParser(props);
            } else {
                options.initPrepareOptionParser(props);
            }
        }

        public Options getBulkImportOptions() {
            return options;
        }

        public PrepareConfiguration newPrepareConfiguration(String[] args) {
            options.setOptions(args);
            OptionSet optionSet = options.getOptions();

            // TODO FIXME when uploadParts is called, default format is "msgpack.gz"
            // on the other hand, when prepareParts, default format is "csv".
            String formatStr;
            if (optionSet.has(BI_PREPARE_PARTS_FORMAT)) {
                formatStr = (String) optionSet.valueOf(BI_PREPARE_PARTS_FORMAT);
            } else {
                formatStr = BI_PREPARE_PARTS_FORMAT_DEFAULTVALUE;
            }

            // lookup format enum
            Format format = Format.fromString(formatStr);
            if (format == null) {
                throw new IllegalArgumentException(String.format(
                        "unsupported format '%s'", formatStr));
            }
            PrepareConfiguration c = format.createPrepareConfiguration();
            c.options = options;
            return c;
        }
    }

    public static enum Format {
        CSV("csv") {
            @Override
            public RecordReader<CSVPrepareConfiguration> createFileReader(
                    PrepareConfiguration conf, RecordWriter writer)
                    throws PreparePartsException {
                return new CSVRecordReader((CSVPrepareConfiguration) conf, writer);
            }

            @Override
            public PrepareConfiguration createPrepareConfiguration() {
                return new CSVPrepareConfiguration();
            }
        },
        TSV("tsv") {
            @Override
            public RecordReader<CSVPrepareConfiguration> createFileReader(
                    PrepareConfiguration conf, RecordWriter writer)
                    throws PreparePartsException {
                return new CSVRecordReader((CSVPrepareConfiguration) conf, writer);
            }

            @Override
            public PrepareConfiguration createPrepareConfiguration() {
                return new CSVPrepareConfiguration();
            }
        },
        MYSQL("mysql") {
            @Override
            public RecordReader<MySQLPrepareConfiguration> createFileReader(
                    PrepareConfiguration conf, RecordWriter writer)
                    throws PreparePartsException {
                return new MySQLTableReader((MySQLPrepareConfiguration) conf,
                        writer);
            }

            @Override
            public PrepareConfiguration createPrepareConfiguration() {
                return new MySQLPrepareConfiguration();
            }
        },
        JSON("json") {
            @Override
            public RecordReader<JSONPrepareConfiguration> createFileReader(
                    PrepareConfiguration conf, RecordWriter writer)
                    throws PreparePartsException {
                return new JSONRecordReader((JSONPrepareConfiguration) conf,
                        writer);
            }

            @Override
            public PrepareConfiguration createPrepareConfiguration() {
                return new JSONPrepareConfiguration();
            }
        },
        REGEX("regex") {
            @Override
            public RecordReader<RegexPrepareConfiguration> createFileReader(
                    PrepareConfiguration conf, RecordWriter writer)
                    throws PreparePartsException {
                return new RegexRecordReader<RegexPrepareConfiguration>(
                        (RegexPrepareConfiguration) conf, writer);
            }

            @Override
            public PrepareConfiguration createPrepareConfiguration() {
                return new RegexPrepareConfiguration();
            }
        },
        APACHE("apache") {
            @Override
            public RecordReader<ApachePrepareConfiguration> createFileReader(
                    PrepareConfiguration conf, RecordWriter writer)
                    throws PreparePartsException {
                return new ApacheRecordReader((ApachePrepareConfiguration) conf,
                        writer);
            }

            @Override
            public PrepareConfiguration createPrepareConfiguration() {
                return new ApachePrepareConfiguration();
            }
        },
        SYSLOG("syslog") {
            @Override
            public RecordReader<SyslogPrepareConfiguration> createFileReader(
                    PrepareConfiguration conf, RecordWriter writer)
                    throws PreparePartsException {
                return new SyslogRecordReader((SyslogPrepareConfiguration) conf,
                        writer);
            }

            @Override
            public PrepareConfiguration createPrepareConfiguration() {
                return new SyslogPrepareConfiguration();
            }
        },
        MSGPACK("msgpack") {
            @Override
            public RecordReader<MessagePackPrepareConfiguration> createFileReader(
                    PrepareConfiguration conf, RecordWriter writer)
                    throws PreparePartsException {
                return new MessagePackRecordReader(
                        (MessagePackPrepareConfiguration) conf, writer);
            }

            @Override
            public PrepareConfiguration createPrepareConfiguration() {
                return new MessagePackPrepareConfiguration();
            }
        };

        private String format;

        Format(String format) {
            this.format = format;
        }

        public String format() {
            return format;
        }

        public abstract PrepareConfiguration createPrepareConfiguration();

        public RecordReader<? extends PrepareConfiguration> createFileReader(
                PrepareConfiguration conf, RecordWriter writer)
                throws PreparePartsException {
            throw new PreparePartsException(
                    new UnsupportedOperationException("format: " + this));
        }

        public static Format fromString(String format) {
            return StringToFormat.get(format);
        }

        private static class StringToFormat {
            private static final Map<String, Format> REVERSE_DICTIONARY;

            static {
                Map<String, Format> map = new HashMap<String, Format>();
                for (Format elem : Format.values()) {
                    map.put(elem.format(), elem);
                }
                REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
            }

            static Format get(String key) {
                return REVERSE_DICTIONARY.get(key);
            }
        }
    }

    public static enum OutputFormat {
        MSGPACKGZ("msgpackgz") {
            @Override
            public RecordWriter createFileWriter(PrepareConfiguration conf) throws PreparePartsException {
                if (!conf.getFormat().equals(Format.MYSQL)) {
                    return new MsgpackGZIPRecordWriter(conf);
                } else {
                    return new MySQLTimestampAdaptedMsgpackGZIPRecordWriter(conf);
                }
            }
        },
        SYSLOGMSGPACKGZ("syslogmsgpackgz") {
            @Override
            public RecordWriter createFileWriter(PrepareConfiguration conf) throws PreparePartsException {
                return new SyslogRecordReader.ExtFileWriter(conf);
            }
        };

        private String outputFormat;

        OutputFormat(String outputFormat) {
            this.outputFormat = outputFormat;
        }

        public String outputFormat() {
            return outputFormat;
        }

        public RecordWriter createFileWriter(PrepareConfiguration conf) throws PreparePartsException {
            throw new PreparePartsException(
                    new UnsupportedOperationException("output format: " + this));
        }

        public static OutputFormat fromString(String outputFormat) {
            return StringToOutputFormat.get(outputFormat);
        }

        private static class StringToOutputFormat {
            private static final Map<String, OutputFormat> REVERSE_DICTIONARY;

            static {
                Map<String, OutputFormat> map = new HashMap<String, OutputFormat>();
                for (OutputFormat elem : OutputFormat.values()) {
                    map.put(elem.outputFormat(), elem);
                }
                REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
            }

            static OutputFormat get(String key) {
                return REVERSE_DICTIONARY.get(key);
            }
        }
    }

    public static enum CompressionType {
        GZIP("gzip") {
            @Override
            public InputStream createInputStream(InputStream in) throws IOException {
                return new BufferedInputStream(new GZIPInputStream(in));
            }
        }, AUTO("auto") {
            @Override
            public InputStream createInputStream(InputStream in) throws IOException {
                throw new IOException("unsupported compress type");
            }
        }, NONE("none") {
            @Override
            public InputStream createInputStream(InputStream in) throws IOException {
                return new BufferedInputStream(in);
            }
        };

        private String type;

        CompressionType(String type) {
            this.type = type;
        }

        public String type() {
            return type;
        }

        public abstract InputStream createInputStream(InputStream in) throws IOException;

        public static CompressionType fromString(String type) {
            return StringToCompressionType.get(type);
        }

        private static class StringToCompressionType {
            private static final Map<String, CompressionType> REVERSE_DICTIONARY;

            static {
                Map<String, CompressionType> map = new HashMap<String, CompressionType>();
                for (CompressionType elem : CompressionType.values()) {
                    map.put(elem.type(), elem);
                }
                REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
            }

            static CompressionType get(String key) {
                return REVERSE_DICTIONARY.get(key);
            }
        }
    }

    public static enum ErrorRecordsHandling {
        SKIP(BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING_DEFAULTVALUE) {
            @Override
            public void handleError(PreparePartsException e)
                    throws PreparePartsException {
                // ignore
            }
        },
        ABORT("abort") {
            @Override
            public void handleError(PreparePartsException e)
                    throws PreparePartsException {
                throw e;
            }
        };

        private String mode;

        ErrorRecordsHandling(String mode) {
            this.mode = mode;
        }

        public String mode() {
            return mode;
        }

        public abstract void handleError(PreparePartsException e)
                throws PreparePartsException;

        public static ErrorRecordsHandling fromString(String mode) {
            return StringToErrorHandling.get(mode);
        }

        private static class StringToErrorHandling {
            private static final Map<String, ErrorRecordsHandling> REVERSE_DICTIONARY;

            static {
                Map<String, ErrorRecordsHandling> map = new HashMap<String, ErrorRecordsHandling>();
                for (ErrorRecordsHandling elem : ErrorRecordsHandling.values()) {
                    map.put(elem.mode(), elem);
                }
                REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
            }

            static ErrorRecordsHandling get(String key) {
                return REVERSE_DICTIONARY.get(key);
            }
        }
    }

    public static enum InvalidColumnsHandling {
        AUTOFIX("autofix") {
            @Override
            public String handleInvalidColumn(String column, int index) {
                String fixed = fixColumnFormat(column, index);
                if (!alreadyHandled(column)) {
                    String msg = "fixed invalid column name: column must contain only lowercase letters, digits, and '_': "
                            + "'" + column + "' is replaced into '" + fixed + "'.";
                    LOG.warning(msg);
                    System.out.println(msg);
                    handleNow(column);
                }
                return fixed;
            }
        },
        WARN(BI_PREPARE_INVALID_COLUMNS_HANDLING_DEFAULTVALUE) {
            @Override
            public String handleInvalidColumn(String column, int index) {
                if (!alreadyHandled(column)) {
                    String msg = "detected invalid column name: column must contain only lowercase letters, digits, and '_': "
                            + "'" + column + "' cannot be used within query strings.";
                    LOG.warning(msg);
                    System.out.println(msg);
                    handleNow(column);
                }
                return column;
            }
        };

        private String mode;
        protected Set<String> cache = new HashSet<String>();

        boolean alreadyHandled(String column) {
            return cache.contains(column);
        }

        void handleNow(String column) {
            cache.add(column);
        }

        InvalidColumnsHandling(String mode) {
            this.mode = mode;
        }

        public String mode() {
            return mode;
        }

        public abstract String handleInvalidColumn(String column, int index);

        public static boolean validColumnFormat(String column) {
            if (column == null || column.isEmpty()) {
                return false;
            }

            for (int i = 0; i < column.length(); i++) {
                int c = column.charAt(i);

                if (i == 0) {
                    if (!isLetter(c) && c != '_') {
                        return false;
                    }
                }

                if (!isDigit(c) && !isLetter(c) && c != '_') {
                    return false;
                }
            }

            return true;
        }

        public static String fixColumnFormat(String column, int index) {
            if (column == null || column.isEmpty()) {
                return "_c" + index;
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < column.length(); i++) {
                int c = column.charAt(i);

                if (isDigit(c) || isLetter(c) || c == '_') {
                    sb.append((char)c);
                } else if (isUpperLetter(c)) {
                    // if upper letter, it is translated into the lower case.
                    sb.append((char)(c + 32));
                } else {
                    // otherwise, '_' is appended
                    sb.append('_');
                }
            }

            return sb.toString();
        }

        private static boolean isDigit(int c) {
            return '0' <= c && c <= '9';
        }

        private static boolean isLetter(int c) {
            return 'a' <= c && c <= 'z';
        }

        private static boolean isUpperLetter(int c) {
            return 'A' <= c && c <= 'Z';
        }

        public static InvalidColumnsHandling fromString(String mode) {
            return StringToInvalidColumnsHandling.get(mode);
        }

        private static class StringToInvalidColumnsHandling {
            private static final Map<String, InvalidColumnsHandling> REVERSE_DICTIONARY;

            static {
                Map<String, InvalidColumnsHandling> map = new HashMap<String, InvalidColumnsHandling>();
                for (InvalidColumnsHandling elem : InvalidColumnsHandling.values()) {
                    map.put(elem.mode(), elem);
                }
                REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
            }

            static InvalidColumnsHandling get(String key) {
                return REVERSE_DICTIONARY.get(key);
            }
        }
    }

    private static final Logger LOG = Logger
            .getLogger(PrepareConfiguration.class.getName());

    // FIXME this field is also declared in td-client.Config.
    protected Properties props;
    protected Options options;
    protected OptionSet optionSet;

    protected Format format;
    protected OutputFormat outputFormat = OutputFormat.MSGPACKGZ;
    protected CompressionType compressionType;
    protected String encoding;
    protected int numOfPrepareThreads;

    protected String aliasTimeColumn;
    protected TimeValueTimeColumnValue timeValue = new TimeValueTimeColumnValue(-1);
    protected String timeFormat;

    protected boolean hasPrimaryKey = false;
    protected String primaryKey = null;
    protected ColumnType primaryKeyType = null;

    protected String errorRecordOutputDirName;
    protected ErrorRecordsHandling errorRecordsHandling;
    protected InvalidColumnsHandling invalidColumnsHandling;
    protected boolean dryRun = false;
    protected String outputDirName;
    protected String errorRecordsOutputDirName;
    protected int splitSize;
    protected int sampleRowSize;

    protected String[] actualColumnNames;
    protected String[] columnNames;
    protected ColumnType[] columnTypes;
    protected Map<String, ColumnType> columnTypeMap = new HashMap<String, ColumnType>();
    protected boolean hasEmptyasNull = false;
    protected boolean hasAllString = false;
    protected String[] excludeColumns;
    protected String[] onlyColumns;

    public PrepareConfiguration() {
    }

    public void configure(Properties props, Options options) {
        this.props = props;
        this.options = options;
        this.optionSet = options.getOptions();

        // format
        setFormat();

        // output format
        setOutputFormat();

        // compression type
        setCompressionType();

        // parallel
        setPrepareThreadNum();

        // error handling
        setErrorRecordsHandling();

        // invalid columns handling
        setInvalidColumnsHandling();

        // encoding
        setEncoding(); // depends on error-records-handling

        // primary key
        setPrimaryKey();

        // alias time column
        setAliasTimeColumn();

        // time value
        setTimeValue();

        // time format
        setTimeFormat();

        // output DIR
        setOutputDirName();

        // output DIR
        setErrorRecordsOutputDirName(); // depends on output

        // all-string
        setAllString();

        // empty-as-null
        setEmptyAsNull();

        // exclude-columns
        setExcludeColumns();

        // only-columns
        setOnlyColumns();

        // dry-run mode
        setDryRun();

        // split size
        setSplitSize();

        // row size with sample reader
        setSampleReaderRowSize();
    }

    public List<String> getNonOptionArguments() {
        return (List<String>) options.getOptions().nonOptionArguments();
    }

    public boolean hasHelpOption() {
        return options.getOptions().has(BI_PREPARE_PARTS_HELP);
    }

    @Override
    public String showHelp(Properties props) {
        StringBuilder sbuf = new StringBuilder();

        // usage
        sbuf.append("usage:\n");
        sbuf.append(Configuration.CMD_PREPARE_USAGE);
        sbuf.append("\n");

        // example
        sbuf.append("example:\n");
        sbuf.append(Configuration.CMD_PREPARE_EXAMPLE);
        sbuf.append("\n");

        // description
        sbuf.append("description:\n");
        sbuf.append(Configuration.CMD_PREPARE_DESC);
        sbuf.append("\n");

        // options
        sbuf.append("options:\n");
        sbuf.append(Configuration.CMD_PREPARE_OPTIONS);
        sbuf.append("\n");

        return sbuf.toString();
    }

    public void setFormat() {
        String formatStr;
        if (!optionSet.has(BI_PREPARE_PARTS_FORMAT)) {
            formatStr = Configuration.BI_PREPARE_PARTS_FORMAT_DEFAULTVALUE;
        } else {
            formatStr = (String) optionSet.valueOf(BI_PREPARE_PARTS_FORMAT);
        }
        format = Format.fromString(formatStr);
        if (format == null) {
            throw new IllegalArgumentException(String.format(
                    "unsupported format '%s'", formatStr));
        }
    }

    public Format getFormat() {
        return format;
    }
    
    public String getSourceTargetDescr() {
    	if(format == Format.MYSQL)
    		return "the source MySQL table";
    	
    	String out = "at least one source ";
        if (format == Format.TSV)
    		return out + "TSV file";
    	else if (format == Format.JSON)
    		return out + "JSON file";
    	else // Format.CSV
    		return out + "CSV file";
    }

    public void setOutputFormat() {
        if (format == null) {
            throw new IllegalStateException(
                    "this method MUST be called after invoking the setFormat()");
        }

        if (format.equals(Format.SYSLOG)) {
            // if format type is 'syslog', output format 
            outputFormat = OutputFormat.SYSLOGMSGPACKGZ;
        } else {
            outputFormat = OutputFormat.MSGPACKGZ;
        }
    }

    public OutputFormat getOutputFormat() {
        return outputFormat;
    }

    public void setCompressionType() {
        String type;
        if (!optionSet.has(BI_PREPARE_PARTS_COMPRESSION)) {
            type = BI_PREPARE_PARTS_COMPRESSION_DEFAULTVALUE;
        } else {
            type = (String) optionSet.valueOf(BI_PREPARE_PARTS_COMPRESSION);
        }

        compressionType = CompressionType.fromString(type);
        if (compressionType == null) {
            throw new IllegalArgumentException(String.format(
                    "unsupported compression type: %s", type));
        }
    }

    public CompressionType getCompressionType() {
        return compressionType;
    }

    public CompressionType checkCompressionType(Source source) throws PreparePartsException {
        if (getCompressionType() != CompressionType.AUTO) {
            return getCompressionType();
        }

        CompressionType[] candidateCompressTypes = new CompressionType[] {
                CompressionType.GZIP, CompressionType.NONE,
        };

        CompressionType compressionType = null;
        for (int i = 0; i < candidateCompressTypes.length; i++) {
            InputStream in = null;
            try {
                if (candidateCompressTypes[i].equals(CompressionType.GZIP)) {
                    in = CompressionType.GZIP.createInputStream(source.getInputStream());
                } else if (candidateCompressTypes[i].equals(CompressionType.NONE)) {
                    in = CompressionType.NONE.createInputStream(source.getInputStream());
                } else {
                    throw new PreparePartsException("fatal error");
                }
                byte[] b = new byte[2];
                in.read(b);

                compressionType = candidateCompressTypes[i];
                break;
            } catch (IOException e) {
                LOG.fine(String.format("source %s is %s", source, e.getMessage()));
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }

        this.compressionType = compressionType;
        return compressionType;
    }

    public void setPrepareThreadNum() {
        String num;
        if (!optionSet.has(BI_PREPARE_PARTS_PARALLEL)) {
            num = BI_PREPARE_PARTS_PARALLEL_DEFAULTVALUE;
        } else {
            num = (String) optionSet.valueOf(BI_PREPARE_PARTS_PARALLEL);
        }

        try {
            int n = Integer.parseInt(num);
            if (n < 0) {
                numOfPrepareThreads = 2;
            } else if (n > 96) {
                numOfPrepareThreads = 96;
            } else {
                numOfPrepareThreads = n;
            }
        } catch (NumberFormatException e) {
            String msg = String.format(
                    "'int' value is required as '%s' option", BI_PREPARE_PARTS_PARALLEL);
            throw new IllegalArgumentException(msg, e);
        }
    }

    public int getNumOfPrepareThreads() {
        return numOfPrepareThreads;
    }

    public void setEncoding() {
        if (!optionSet.has(BI_PREPARE_PARTS_ENCODING)) {
            encoding = BI_PREPARE_PARTS_ENCODING_DEFAULTVALUE;
        } else {
            encoding = (String) optionSet.valueOf(BI_PREPARE_PARTS_ENCODING);
        }
    }

    public CharsetDecoder getCharsetDecoder() throws PreparePartsException {
        return charsetDecoders.get();
    }

    private ThreadLocal<CharsetDecoder> charsetDecoders = new ThreadLocal<CharsetDecoder>() {
        @Override
        protected CharsetDecoder initialValue() {
            CharsetDecoder charsetDecoder = Charset.forName(encoding).newDecoder();
            if (errorRecordsHandling.equals(ErrorRecordsHandling.ABORT)) {
                charsetDecoder.onMalformedInput(CodingErrorAction.REPORT);
                charsetDecoder.onUnmappableCharacter(CodingErrorAction.REPORT);
            } else { // skip
                charsetDecoder.onMalformedInput(CodingErrorAction.REPLACE);
                charsetDecoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
            }
            return charsetDecoder;
        }
    };

    public void setPrimaryKey() {
        if (!optionSet.has(BI_PREPARE_PARTS_PRIMARY_KEY)) {
            return;
        }

        // if 'primary-key' option appears, ....
        String pair = (String) optionSet.valueOf(BI_PREPARE_PARTS_PRIMARY_KEY);
        if (pair.indexOf(":") <= 0) {
            throw new IllegalArgumentException(
                    String.format("Invalid 'primary-key' option: %s", pair));
        }

        String[] nameAndType = pair.split(":");
        if (nameAndType.length != 2) {
            throw new IllegalArgumentException(
                    String.format("Invalid 'primary-key' option: %s", pair));
        }

        String name = nameAndType[0];
        ColumnType type = ColumnType.Conv.fromString(nameAndType[1]);
        if (type == null || !(type.equals(ColumnType.INT) || type.equals(ColumnType.STRING))) {
            throw new IllegalArgumentException(String.format(
                    "primary-key's type must be 'int' or 'string' only: %s", pair));
        }

        primaryKey = name;
        primaryKeyType = type;
        hasPrimaryKey = true;
    }

    public boolean hasPrimaryKey() {
        return hasPrimaryKey;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public ColumnType getPrimaryKeyType() {
        return primaryKeyType;
    }

    public void setAliasTimeColumn() {
        if (optionSet.has(BI_PREPARE_PARTS_TIMECOLUMN)) {
            if (hasPrimaryKey()) {
                throw new IllegalArgumentException(
                        "cannot specify both of 'time-column' and 'primary-key' options");
            }
            aliasTimeColumn = (String) optionSet.valueOf(BI_PREPARE_PARTS_TIMECOLUMN);
        }
    }

    public String getAliasTimeColumn() {
        return aliasTimeColumn;
    }

    public void setTimeValue() {
        if (!optionSet.has(BI_PREPARE_PARTS_TIMEVALUE)) {
            return;
        }

        if (hasPrimaryKey()) {
            throw new IllegalArgumentException(
                    "cannot specify both of 'time-value' and 'primary-key' options");
        }

        String v = (String) optionSet.valueOf(BI_PREPARE_PARTS_TIMEVALUE);
        if (v != null) {
            boolean periodicallySorted = v.indexOf(',') >= 0;
            try {
                if (!periodicallySorted) {
                    timeValue = new TimeValueTimeColumnValue(Long.parseLong(v));
                } else {
                    String[] vv = v.split(",");
                    if (vv.length != 2) {
                        throw new IllegalArgumentException(
                                "'time-value' option requires a pair argument of long typed unix time and hours sorted periodically <unixtime>,<hours> like 1394409600,10");
                    }
                    timeValue = new TimeValueTimeColumnValue(
                            Long.parseLong(vv[0]), Long.parseLong(vv[1]));
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "'time-value option requires the long type argument'", e);
            }
        }
    }

    public TimeValueTimeColumnValue getTimeValue() {
        return timeValue;
    }

    public void setTimeFormat() {
        if (optionSet.has(BI_PREPARE_PARTS_TIMEFORMAT)) {
            if (hasPrimaryKey()) {
                throw new IllegalArgumentException(
                        "cannot specify both of 'time-format' and 'primary-key' options");
            }
            timeFormat = (String) optionSet.valueOf(BI_PREPARE_PARTS_TIMEFORMAT);
        }
    }

    public Strftime getTimeFormat() {
        return timeFormat == null ? null : new Strftime(timeFormat);
    }

    public Strftime getTimeFormat(String strfString) {
        return strfString == null ? null : new Strftime(strfString);
    }

    public void setOutputDirName() {
        if (optionSet.has("output")) {
            outputDirName = (String) optionSet.valueOf(BI_PREPARE_PARTS_OUTPUTDIR);
        }

        File outputDir = null;
        if (outputDirName == null || outputDirName.isEmpty()) {
            outputDir = new File(new File("."), BI_PREPARE_PARTS_OUTPUTDIR_DEFAULTVALUE);
            outputDirName = outputDir.getName();
        } else {
            outputDir = new File(outputDirName);
        }

        // validate output dir
        if (!outputDir.isDirectory()) {
            if (!outputDir.mkdir()) {
                throw new IllegalArgumentException(String.format(
                        "Cannot create '%s' directory '%s'",
                        BI_PREPARE_PARTS_OUTPUTDIR, outputDirName));
            }
        }
    }

    public void setErrorRecordsOutputDirName() {
        if (optionSet.has(BI_PREPARE_PARTS_ERROR_RECORDS_OUTPUT)) {
            errorRecordsOutputDirName = (String) optionSet.valueOf(BI_PREPARE_PARTS_ERROR_RECORDS_OUTPUT);
        }

        File errorRecordsOutputDir = null;
        if (errorRecordsOutputDirName == null || errorRecordsOutputDirName.isEmpty()) {
            errorRecordsOutputDir = new File(new File("."), BI_PREPARE_PARTS_ERROR_RECORDS_OUTPUTDIR_DEFAULTVALUE);
            errorRecordsOutputDirName = errorRecordsOutputDir.getName();
        } else {
            errorRecordsOutputDir = new File(errorRecordsOutputDirName);
        }

        // validate dir
        if (!errorRecordsOutputDir.isDirectory()) {
            if (!errorRecordsOutputDir.mkdir()) {
                throw new IllegalArgumentException(String.format(
                        "Cannot create '%s' directory '%s'",
                        BI_PREPARE_PARTS_ERROR_RECORDS_OUTPUT, errorRecordsOutputDirName));
            }
        }
    }

    public void setErrorRecordsHandling() {
        String mode;
        if (!optionSet.has(BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING)) {
            mode = BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING_DEFAULTVALUE;
        } else {
            mode = (String) optionSet.valueOf(BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING);
        }

        errorRecordsHandling = ErrorRecordsHandling.fromString(mode);
        if (errorRecordsHandling == null) {
            throw new IllegalArgumentException(String.format(
                    "unsupported '%s' mode '%s'",
                    BI_PREPARE_PARTS_ERROR_RECORDS_HANDLING, mode));
        }
    }

    public ErrorRecordsHandling getErrorRecordsHandling() {
        return errorRecordsHandling;
    }

    public void setInvalidColumnsHandling() {
        String mode;
        if (!optionSet.has(BI_PREPARE_INVALID_COLUMNS_HANDLING)) {
            mode = BI_PREPARE_INVALID_COLUMNS_HANDLING_DEFAULTVALUE;
        } else {
            mode = (String) optionSet.valueOf(BI_PREPARE_INVALID_COLUMNS_HANDLING);
        }

        invalidColumnsHandling = InvalidColumnsHandling.fromString(mode);
        if (invalidColumnsHandling == null) {
            throw new IllegalArgumentException(String.format(
                    "unsupported '%s' mode '%s'",
                    BI_PREPARE_INVALID_COLUMNS_HANDLING, mode));
        }
    }

    public InvalidColumnsHandling getInvalidColumnsHandling() {
        return invalidColumnsHandling;
    }

    public void setDryRun() {
        if (optionSet.has("dry-run")) {
            String drun = (String) optionSet.valueOf("dry-run");
            dryRun = drun != null && drun.equals("true");    
        }
    }

    public boolean dryRun() {
        return dryRun;
    }

    public String getOutputDirName() {
        return outputDirName;
    }

    public String getErrorRecordsOutputDirName() {
        return errorRecordsOutputDirName;
    }

    public void setSplitSize() {
        String size;
        if (!optionSet.has(BI_PREPARE_PARTS_SPLIT_SIZE)) {
            size = BI_PREPARE_PARTS_SPLIT_SIZE_DEFAULTVALUE;
        } else {
            size = (String) optionSet.valueOf(BI_PREPARE_PARTS_SPLIT_SIZE);
        }

        try {
            splitSize = Integer.parseInt(size);
        } catch (NumberFormatException e) {
            String msg = String.format("'%s' is required as int type",
                    BI_PREPARE_PARTS_SPLIT_SIZE);
            throw new IllegalArgumentException(msg, e);
        }
    }

    public int getSplitSize() {
        return splitSize;
    }

    public void setSampleReaderRowSize() {
        String sRowSize = props.getProperty(
                Configuration.BI_PREPARE_PARTS_SAMPLE_ROWSIZE,
                Configuration.BI_PREPARE_PARTS_SAMPLE_ROWSIZE_DEFAULTVALUE);
        try {
            sampleRowSize = Integer.parseInt(sRowSize);
        } catch (NumberFormatException e) {
            String msg = String.format(
                    "sample row size is required as int type e.g. -D%s=%s",
                    Configuration.BI_PREPARE_PARTS_SAMPLE_ROWSIZE,
                    Configuration.BI_PREPARE_PARTS_SAMPLE_ROWSIZE_DEFAULTVALUE);
            throw new IllegalArgumentException(msg, e);
        }
    }

    public int getSampleRowSize() {
        return sampleRowSize;
    }

    public void setColumnNames() {
        if (!optionSet.has(BI_PREPARE_PARTS_COLUMNS)) {
            actualColumnNames = new String[0];
            columnNames = new String[0];
        } else {
            String[] cnames = optionSet.valuesOf(BI_PREPARE_PARTS_COLUMNS).toArray(new String[0]);
            setColumnNames(cnames);
        }
    }

    public void setColumnNames(String[] cnames) {
        this.actualColumnNames = cnames;
        String[] cnames0 = new String[cnames.length];
        for (int i = 0; i < cnames.length; i++) {
            if (InvalidColumnsHandling.validColumnFormat(cnames[i])) {
                cnames0[i] = cnames[i];
            } else {
                cnames0[i] = invalidColumnsHandling.handleInvalidColumn(cnames[i], i);
            }
        }
        this.columnNames = cnames0;
    }

    public String[] getActualColumnNames() {
        return actualColumnNames;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public void setColumnTypes() {
        if (!optionSet.has(BI_PREPARE_PARTS_COLUMNTYPES)) {
            columnTypes = new ColumnType[0];
        } else {
            String[] types = optionSet.valuesOf(BI_PREPARE_PARTS_COLUMNTYPES).toArray(new String[0]);
            columnTypes = new ColumnType[types.length];
            for (int i = 0; i < types.length; i++) {
                columnTypes[i] = ColumnType.Conv.fromString(types[i].toLowerCase());
                if (columnTypes[i] == null) {
                    throw new IllegalArgumentException(String.format(
                            "'%s' cannot be specified as column type", types[i]));
                }
            }
        }
    }

    public void setColumnTypes(ColumnType[] columnTypes) {
        this.columnTypes = columnTypes;
    }

    public ColumnType[] getColumnTypes() {
        return columnTypes;
    }

    public void setColumnTypeMap() {
        if (!optionSet.has(BI_PREPARE_COLUMNTYPE)) {
            return;
        }

        List<?> args = optionSet.valuesOf(BI_PREPARE_COLUMNTYPE);
        if (args == null || args.isEmpty()) {
            return;
        }

        columnTypeMap.clear();
        Iterator<?> argsIter = args.iterator();
        while (argsIter.hasNext()) {
            String arg = (String) argsIter.next();
            int i = arg.indexOf(":");
            if (i <= 0) {
                throw new IllegalArgumentException(
                        String.format("Invalid 'column-type' option: %s", arg));
            }
            String[] nameAndType = arg.split(":");
            if (nameAndType.length != 2) {
                throw new IllegalArgumentException(
                        String.format("Invalid 'column-type' option: %s", arg));
            }

            String name = nameAndType[0];
            ColumnType type = ColumnType.Conv.fromString(nameAndType[1]);
            if (type != null) {
                columnTypeMap.put(name, type);
            }
        }
    }

    public Map<String, ColumnType> getColumnTypeMap() {
        return columnTypeMap;
    }

    public void setAllString() {
        hasAllString = optionSet.has(BI_PREPARE_ALL_STRING);
    }

    public boolean hasAllString() {
        return hasAllString;
    }

    //@ForTesting
    public void setEmptyAsNull(boolean flag) {
        hasEmptyasNull = flag;
    }

    public void setEmptyAsNull() {
        hasEmptyasNull = optionSet.has(BI_PREPARE_PARTS_EMPTYASNULL);
    }

    public boolean hasEmptyAsNull() {
        return hasEmptyasNull;
    }

    public void setExcludeColumns() {
        if (!optionSet.has(BI_PREPARE_PARTS_EXCLUDE_COLUMNS)) {
            excludeColumns = new String[0];
        } else {
            excludeColumns = optionSet.valuesOf(BI_PREPARE_PARTS_EXCLUDE_COLUMNS).toArray(new String[0]);
            for (String c : excludeColumns) {
                if (!hasPrimaryKey()) {
                    if (c.equals(Configuration.BI_PREPARE_PARTS_TIMECOLUMN_DEFAULTVALUE)) {
                        throw new IllegalArgumentException(String.format(
                                "'time' column cannot be included in '%s'",
                                BI_PREPARE_PARTS_EXCLUDE_COLUMNS));
                    }
                } else {
                    if (c.equals(getPrimaryKey())) {
                        throw new IllegalArgumentException(String.format(
                                "'primary-key' column cannot be included in '%s'",
                                BI_PREPARE_PARTS_EXCLUDE_COLUMNS));
                    }
                }
            }
        }
    }

    public String[] getExcludeColumns() {
        return excludeColumns;
    }

    public void setOnlyColumns() {
        if (!optionSet.has(BI_PREPARE_PARTS_ONLY_COLUMNS)) {
            onlyColumns = new String[0];
        } else {
            onlyColumns = optionSet.valuesOf(BI_PREPARE_PARTS_ONLY_COLUMNS).toArray(new String[0]);
            for (String oc : onlyColumns) {
                for (String ec : excludeColumns) {
                    if (oc.equals(ec)) {
                        throw new IllegalArgumentException(String.format(
                                "don't include '%s' in '%s'",
                                BI_PREPARE_PARTS_EXCLUDE_COLUMNS,
                                BI_PREPARE_PARTS_ONLY_COLUMNS));
                    }
                }
            }
        }
    }

    public String[] getOnlyColumns() {
        return onlyColumns;
    }

}
