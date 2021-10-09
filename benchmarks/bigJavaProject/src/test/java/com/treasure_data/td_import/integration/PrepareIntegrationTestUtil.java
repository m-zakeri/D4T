package com.treasure_data.td_import.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import com.treasure_data.td_import.BulkImportCommand;
import com.treasure_data.td_import.Configuration;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.MapValue;
import org.msgpack.value.Value;
import org.msgpack.value.ValueFactory;

@Ignore
public class PrepareIntegrationTestUtil {
    private static Value STRING_VALUE = ValueFactory.newString("string_value");
    private static Value INT_VALUE = ValueFactory.newString("int_value");
    private static Value DOUBLE_VALUE = ValueFactory.newString("double_value");
    private static Value TIME = ValueFactory.newString("time");

    static final String INPUT_DIR = "./src/test/resources/in/";
    static final String OUTPUT_DIR = "./src/test/resources/out/";

    protected Properties props;
    protected List<String> opts;
    protected List<String> args;

    protected BulkImportCommand command;

    @Before
    public void createResources() throws Exception {
        props = new Properties();
        opts = new ArrayList<String>();
        args = new ArrayList<String>();

        command = new BulkImportCommand(props);
    }

    @After
    public void destroyResources() throws Exception {
    }

    public void setItemTableOptions(String format, boolean columnHeader,
            String primaryKey, String columnNames, String exclude, String only) {
        // format
        if (format != null && !format.isEmpty()) {
            opts.add("--format");
            opts.add(format);
        }

        // output dir
        opts.add("--output");
        opts.add(OUTPUT_DIR);

        // column header
        if (columnHeader) {
            opts.add("--column-header");
        }

        // primary-key
        opts.add("--primary-key");
        opts.add(primaryKey);

        // column names
        if (columnNames != null && !columnNames.isEmpty()) {
            opts.add("--columns");
            opts.add(columnNames);
        }

        // exclude columns
        if (exclude != null && !exclude.isEmpty()) {
            opts.add("--exclude-columns");
            opts.add(exclude);
        }

        // only columns
        if (only != null && !only.isEmpty()) {
            opts.add("--only-columns");
            opts.add(only);
        }
    }

    public void refleshOptions() {
        opts.clear();
        props.clear();
        args.clear();
    }
    public void setOptions(String format, boolean columnHeader,
            String aliasTimeColumn, String timeFormat, String columnNames, String exclude, String only) {
        // format
        if (format != null && !format.isEmpty()) {
            opts.add("--format");
            opts.add(format);
        }

        // output dir
        opts.add("--output");
        opts.add(OUTPUT_DIR);

        // column header
        if (columnHeader) {
            opts.add("--column-header");
        }

        // alias time column
        if (aliasTimeColumn != null && !aliasTimeColumn.isEmpty()) {
            opts.add("--time-column");
            opts.add(aliasTimeColumn);
        }

        // time format
        if (timeFormat != null && !timeFormat.isEmpty()) {
            opts.add("--time-format");
            opts.add(timeFormat);
        }

        // column names
        if (columnNames != null && !columnNames.isEmpty()) {
            opts.add("--columns");
            opts.add(columnNames);
        }

        // exclude columns
        if (exclude != null && !exclude.isEmpty()) {
            opts.add("--exclude-columns");
            opts.add(exclude);
        }

        // only columns
        if (only != null && !only.isEmpty()) {
            opts.add("--only-columns");
            opts.add(only);
        }
    }

    public void prepareParts(String fileName) throws Exception {
        args.add(Configuration.CMD_PREPARE);
        args.add(fileName);
        args.addAll(opts);

        command.doPrepareCommand(args.toArray(new String[0]));
    }

    public void preparePartsFromCSVWithTimeColumn() throws Exception {
        prepareParts(INPUT_DIR + "csvfile-with-time.csv");

        String srcFileName = INPUT_DIR + "trainingfile-with-time.msgpack.gz";
        String dstFileName = OUTPUT_DIR + "csvfile-with-time_csv_0.msgpack.gz";
        assertDataEquals(srcFileName, dstFileName);
    }

    public void preparePartsFromCSVWithAlasTimeColumn() throws Exception {
        prepareParts(INPUT_DIR + "csvfile-with-aliastime.csv");

        String srcFileName = INPUT_DIR + "trainingfile-with-time.msgpack.gz";
        String dstFileName = OUTPUT_DIR + "csvfile-with-aliastime_csv_0.msgpack.gz";
        assertDataEquals(srcFileName, dstFileName);
    }

    public void preparePartsFromCSVWithTimeFormat() throws Exception {
        prepareParts(INPUT_DIR + "csvfile-with-timeformat.csv");

        String srcFileName = INPUT_DIR + "trainingfile-with-time.msgpack.gz";
        String dstFileName = OUTPUT_DIR + "csvfile-with-timeformat_csv_0.msgpack.gz";
        assertDataEquals(srcFileName, dstFileName);
    }

    public void preparePartsFromCSVWithSuggestedTimeFormat() throws Exception {
        prepareParts(INPUT_DIR + "csvfile-with-suggested-timeformat.csv");

        String srcFileName = INPUT_DIR + "trainingfile-with-time.msgpack.gz";
        String dstFileName = OUTPUT_DIR + "csvfile-with-suggested-timeformat_csv_0.msgpack.gz";
        assertDataEquals(srcFileName, dstFileName);
    }

    public void preparePartsFromHeaderlessCSVWithTimeColumn() throws Exception {
        prepareParts(INPUT_DIR + "headerless-csvfile-with-time.csv");

        String srcFileName = INPUT_DIR + "trainingfile-with-time.msgpack.gz";
        String dstFileName = OUTPUT_DIR + "headerless-csvfile-with-time_csv_0.msgpack.gz";
        assertDataEquals(srcFileName, dstFileName);
    }

    public void preparePartsFromHeaderlessCSVWithAlasTimeColumn() throws Exception {
        prepareParts(INPUT_DIR + "headerless-csvfile-with-aliastime.csv");

        String srcFileName = INPUT_DIR + "trainingfile-with-time.msgpack.gz";
        String dstFileName = OUTPUT_DIR + "headerless-csvfile-with-aliastime_csv_0.msgpack.gz";
        assertDataEquals(srcFileName, dstFileName);
    }

    public void preparePartsFromHeaderlessCSVWithTimeFormat() throws Exception {
        prepareParts(INPUT_DIR + "headerless-csvfile-with-timeformat.csv");

        String srcFileName = INPUT_DIR + "trainingfile-with-time.msgpack.gz";
        String dstFileName = OUTPUT_DIR + "headerless-csvfile-with-timeformat_csv_0.msgpack.gz";
        assertDataEquals(srcFileName, dstFileName);
    }

    public void preparePartsFromHeaderlessCSVWithSuggestedTimeFormat() throws Exception {
        prepareParts(INPUT_DIR + "headerless-csvfile-with-suggested-timeformat.csv");

        String srcFileName = INPUT_DIR + "trainingfile-with-time.msgpack.gz";
        String dstFileName = OUTPUT_DIR + "headerless-csvfile-with-suggested-timeformat_csv_0.msgpack.gz";
        assertDataEquals(srcFileName, dstFileName);
    }

    public void preparePartsFromTSVWithTimeColumn() throws Exception {
        prepareParts(INPUT_DIR + "tsvfile-with-time.tsv");

        String srcFileName = INPUT_DIR + "trainingfile-with-time.msgpack.gz";
        String dstFileName = OUTPUT_DIR + "tsvfile-with-time_tsv_0.msgpack.gz";
        assertDataEquals(srcFileName, dstFileName);
    }

    public void preparePartsFromTSVWithAlasTimeColumn() throws Exception {
        prepareParts(INPUT_DIR + "tsvfile-with-aliastime.tsv");

        String srcFileName = INPUT_DIR + "trainingfile-with-time.msgpack.gz";
        String dstFileName = OUTPUT_DIR + "tsvfile-with-aliastime_tsv_0.msgpack.gz";
        assertDataEquals(srcFileName, dstFileName);
    }

    public void preparePartsFromTSVWithTimeFormat() throws Exception {
        prepareParts(INPUT_DIR + "tsvfile-with-timeformat.tsv");

        String srcFileName = INPUT_DIR + "trainingfile-with-time.msgpack.gz";
        String dstFileName = OUTPUT_DIR + "tsvfile-with-timeformat_tsv_0.msgpack.gz";
        assertDataEquals(srcFileName, dstFileName);
    }

    public void preparePartsFromHeaderlessTSVWithTimeColumn() throws Exception {
        prepareParts(INPUT_DIR + "headerless-tsvfile-with-time.tsv");

        String srcFileName = INPUT_DIR + "trainingfile-with-time.msgpack.gz";
        String dstFileName = OUTPUT_DIR + "headerless-tsvfile-with-time_tsv_0.msgpack.gz";
        assertDataEquals(srcFileName, dstFileName);
    }

    public void preparePartsFromHeaderlessTSVWithAlasTimeColumn() throws Exception {
        prepareParts(INPUT_DIR + "headerless-tsvfile-with-aliastime.tsv");

        String srcFileName = INPUT_DIR + "trainingfile-with-time.msgpack.gz";
        String dstFileName = OUTPUT_DIR + "headerless-tsvfile-with-aliastime_tsv_0.msgpack.gz";
        assertDataEquals(srcFileName, dstFileName);
    }

    public void preparePartsFromHeaderlessTSVWithTimeFormat() throws Exception {   
        prepareParts(INPUT_DIR + "headerless-tsvfile-with-timeformat.tsv");

        String srcFileName = INPUT_DIR + "trainingfile-with-time.msgpack.gz";
        String dstFileName = OUTPUT_DIR + "headerless-tsvfile-with-timeformat_tsv_0.msgpack.gz";
        assertDataEquals(srcFileName, dstFileName);
    }

    public void preparePartsFromSyslog() throws Exception {
        prepareParts(INPUT_DIR + "syslogfile.syslog");

        String srcFileName = INPUT_DIR + "trainingfile-with-time.msgpack.gz";
        String dstFileName = OUTPUT_DIR + "syslogfile_syslog_0.msgpack.gz";
        assertDataEquals(srcFileName, dstFileName, "syslog");
    }

    public void preparePartsFromApacheLog() throws Exception {
        prepareParts(INPUT_DIR + "apachelogfile.apache");

        String srcFileName = INPUT_DIR + "trainingfile-with-time.msgpack.gz";
        String dstFileName = OUTPUT_DIR + "apachelogfile_apache_0.msgpack.gz";
        assertDataEquals(srcFileName, dstFileName, "apache");
    }

    // TODO
    // TODO

    public void preparePartsFromJSONWithTimeColumn() throws Exception {
        prepareParts(INPUT_DIR + "jsonfile-with-time.json");

        String srcFileName = INPUT_DIR + "trainingfile-with-time.msgpack.gz";
        String dstFileName = OUTPUT_DIR + "jsonfile-with-time_json_0.msgpack.gz";
        assertDataEquals(srcFileName, dstFileName);
    }

    public void preparePartsFromJSONWithAlasTimeColumn() throws Exception {
        prepareParts(INPUT_DIR + "jsonfile-with-aliastime.json");

        String srcFileName = INPUT_DIR + "trainingfile-with-time.msgpack.gz";
        String dstFileName = OUTPUT_DIR + "jsonfile-with-aliastime_json_0.msgpack.gz";
        assertDataEquals(srcFileName, dstFileName);
    }

    public void preparePartsFromJSONWithTimeFormat() throws Exception {
        prepareParts(INPUT_DIR + "jsonfile-with-timeformat.json");

        String srcFileName = INPUT_DIR + "trainingfile-with-time.msgpack.gz";
        String dstFileName = OUTPUT_DIR + "jsonfile-with-timeformat_json_0.msgpack.gz";
        assertDataEquals(srcFileName, dstFileName);
    }

    public void preparePartsFromMessagePackWithTimeColumn() throws Exception {
        prepareParts(INPUT_DIR + "msgpackfile-with-time.msgpack");

        String srcFileName = INPUT_DIR + "trainingfile-with-time.msgpack.gz";
        String dstFileName = OUTPUT_DIR + "msgpackfile-with-time_msgpack_0.msgpack.gz";
        assertDataEquals(srcFileName, dstFileName);
    }

    public void preparePartsFromMessagePackWithAlasTimeColumn() throws Exception {
        prepareParts(INPUT_DIR + "msgpackfile-with-aliastime.msgpack");

        String srcFileName = INPUT_DIR + "trainingfile-with-time.msgpack.gz";
        String dstFileName = OUTPUT_DIR + "msgpackfile-with-aliastime_msgpack_0.msgpack.gz";
        assertDataEquals(srcFileName, dstFileName);
    }

    public void preparePartsFromMessagePackWithTimeFormat() throws Exception {
        prepareParts(INPUT_DIR + "msgpackfile-with-timeformat.msgpack");

        String srcFileName = INPUT_DIR + "trainingfile-with-time.msgpack.gz";
        String dstFileName = OUTPUT_DIR + "msgpackfile-with-timeformat_msgpack_0.msgpack.gz";
        assertDataEquals(srcFileName, dstFileName);
    }

    public void assertDataEquals(String srcFileName, String dstFileName) throws Exception {
        assertDataEquals(srcFileName, dstFileName, "none");
    }

    public void assertDataEquals(String srcFileName, String dstFileName, String format) throws Exception {
        InputStream srcIn = new BufferedInputStream(new GZIPInputStream(new FileInputStream(srcFileName)));
        InputStream dstIn = new BufferedInputStream(new GZIPInputStream(new FileInputStream(dstFileName)));

        MessageUnpacker srcIter = MessagePack.newDefaultUnpacker(srcIn);
        MessageUnpacker dstIter = MessagePack.newDefaultUnpacker(dstIn);

        while (srcIter.hasNext() && dstIter.hasNext()) {
            MapValue srcMap = srcIter.unpackValue().asMapValue();
            MapValue dstMap = dstIter.unpackValue().asMapValue();

            assertMapValueEquals(srcMap, dstMap, format);
        }

        assertFalse(srcIter.hasNext());
        assertFalse(dstIter.hasNext());
    }

    private void assertMapValueEquals(MapValue srcValue, MapValue dstValue, String format) {
        Map<Value, Value> src = srcValue.map();
        Map<Value, Value> dst = dstValue.map();
        if (format.equals("none")) {
            assertTrue(src.containsKey(STRING_VALUE));
            assertEquals(src.get(STRING_VALUE), dst.get(STRING_VALUE));

            assertTrue(src.containsKey(INT_VALUE));
            assertEquals(src.get(INT_VALUE), dst.get(INT_VALUE));

            assertTrue(src.containsKey(DOUBLE_VALUE));
            assertEquals(src.get(DOUBLE_VALUE), dst.get(DOUBLE_VALUE));

            assertTrue(src.containsKey(TIME));
            assertEquals(src.get(TIME), dst.get(TIME));
        } else if (format.equals("syslog")) {
            assertTrue(src.containsKey(STRING_VALUE));
            assertEquals(src.get(STRING_VALUE), dst.get(SyslogFileGenerator.HOST_VALUE));
            assertEquals(src.get(STRING_VALUE), dst.get(SyslogFileGenerator.IDENT_VALUE));
            assertEquals(src.get(STRING_VALUE), dst.get(SyslogFileGenerator.MESSAGE_VALUE));

            assertTrue(src.containsKey(INT_VALUE));
            assertEquals(src.get(INT_VALUE), dst.get(SyslogFileGenerator.PID_VALUE));

            assertTrue(src.containsKey(TIME));
            assertEquals(src.get(TIME), dst.get(TIME));
        } else if (format.equals("apache")) {
            try {
            assertTrue(src.containsKey(STRING_VALUE));
            assertEquals(src.get(STRING_VALUE), dst.get(ApacheFileGenerator.HOST_VALUE));
            assertEquals(src.get(STRING_VALUE), dst.get(ApacheFileGenerator.USER_VALUE));
            assertEquals(src.get(STRING_VALUE), dst.get(ApacheFileGenerator.METHOD_VALUE));
            assertEquals(src.get(STRING_VALUE), dst.get(ApacheFileGenerator.PATH_VALUE));
            assertEquals(src.get(INT_VALUE), dst.get(ApacheFileGenerator.CODE_VALUE));
            assertEquals(src.get(INT_VALUE), dst.get(ApacheFileGenerator.SIZE_VALUE));

            assertTrue(src.containsKey(TIME));
            assertEquals(src.get(TIME), dst.get(TIME));
            } catch (Throwable t) {
                System.out.println("src: " + src);
                System.out.println("dst: " + dst);
                throw new RuntimeException(t);
            }
        } else {
            throw new RuntimeException();
        }
    }
}
