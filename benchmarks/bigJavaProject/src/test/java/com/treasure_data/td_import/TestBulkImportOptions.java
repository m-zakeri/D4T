package com.treasure_data.td_import;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import joptsimple.OptionSet;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.treasure_data.td_import.Options;

public class TestBulkImportOptions {

    private final String sampleFormat = "csv";
    private final String sampleCompress = "gzip";
    private final String sampleEncoding = "udf-8";
    private final String sampleTimeColumn = "timestamp";
    private final String sampleTimeFormat = "timeformat";
    private final String sampleTimeValue = "100";
    private final String sampleOutput = "output_dir";
    private final String sampleSplitSize = "100";
    private final String sampleErrorRecordsHandling = "skip";
    private final String sampleDelimiter = ",";
    private final String sampleQuote = "DOUBLE";
    private final String sampleNewline = "CRLF";
    private final String sampleColumns = "c0,c1,c2";
    private final String sampleColumnTypes = "string,int,int";
    private final String sampleExcludeColumns = "c0,c1,c2";
    private final String sampleOnlyColumns = "c0,c1,c2";
    private final String samplePrepareParallel = "10";
    private final String sampleParallel = "10";

    protected Properties props;
    protected Options actualOpts;

    @Before
    public void createResources() throws Exception {
        props = System.getProperties();
        actualOpts = new Options();
    }

    @Test @Ignore
    public void showHelp() throws Exception {
        actualOpts.showHelp();
    }

    @Test
    public void testPrepareOptions() throws Exception {
        actualOpts.initPrepareOptionParser(props);
        actualOpts.setOptions(createPrepareArguments());
        assertPrepareOptionEquals(actualOpts);
    }

    @Test
    public void testUploadOptions() throws Exception {
        actualOpts.initUploadOptionParser(props);
        actualOpts.setOptions(createUploadArguments());
        assertUploadOptionEquals(actualOpts);
    }

    private String[] createPrepareArguments() {
        return new String[] {
                "--format", sampleFormat,
                "--compress", sampleCompress,
                "--encoding", sampleEncoding,
                "--time-column", sampleTimeColumn,
                "--time-format", sampleTimeFormat,
                "--time-value", sampleTimeValue,
                "--output", sampleOutput,
                "--split-size", sampleSplitSize,
                "--error-records-handling", sampleErrorRecordsHandling,
                "--delimiter", sampleDelimiter,
                "--quote", sampleQuote,
                "--newline", sampleNewline,
                "--column-header",
                "--columns", sampleColumns,
                "--column-types", sampleColumnTypes,
                "--exclude-columns", sampleExcludeColumns,
                "--only-columns", sampleOnlyColumns,
                "--prepare-parallel", samplePrepareParallel
        };
    }

    private String[] createUploadArguments() {
        String[] prepareArgs = createPrepareArguments();
        String[] args = new String[] {
                "--auto-perform",
                "--auto-commit",
                "--parallel", sampleParallel,
        };

        String[] uploadArgs = new String[prepareArgs.length + args.length];
        System.arraycopy(prepareArgs, 0, uploadArgs, 0, prepareArgs.length);
        System.arraycopy(args, 0, uploadArgs, prepareArgs.length, args.length);
        return uploadArgs;
    }

    public void assertPrepareOptionEquals(Options actualOpts) throws Exception {
        assertOptionEquals("f", sampleFormat, actualOpts);
        assertOptionEquals("format", sampleFormat, actualOpts);
        assertOptionEquals("C", sampleCompress, actualOpts);
        assertOptionEquals("compress", sampleCompress, actualOpts);
        assertOptionEquals("e", sampleEncoding, actualOpts);
        assertOptionEquals("encoding", sampleEncoding, actualOpts);
        assertOptionEquals("t", sampleTimeColumn, actualOpts);
        assertOptionEquals("time-column", sampleTimeColumn, actualOpts);
        assertOptionEquals("T", sampleTimeFormat, actualOpts);
        assertOptionEquals("time-format", sampleTimeFormat, actualOpts);
        assertOptionEquals("time-value", sampleTimeValue, actualOpts);
        assertOptionEquals("output", sampleOutput, actualOpts);
        assertOptionEquals("split-size", sampleSplitSize, actualOpts);
        assertOptionEquals("error-records-handling", sampleErrorRecordsHandling, actualOpts);
        assertOptionEquals("delimiter", sampleDelimiter, actualOpts);
        assertOptionEquals("quote", sampleQuote, actualOpts);
        assertOptionEquals("newline", sampleNewline, actualOpts);
        assertOptionEquals("column-header", actualOpts);
        assertOptionEquals("columns", sampleColumns.split(","), actualOpts);
        assertOptionEquals("column-types", sampleColumnTypes.split(","), actualOpts);
        assertOptionEquals("exclude-columns", sampleExcludeColumns.split(","), actualOpts);
        assertOptionEquals("only-columns", sampleOnlyColumns.split(","), actualOpts);
        assertOptionEquals("prepare-parallel", samplePrepareParallel, actualOpts);
    }

    public void assertUploadOptionEquals(Options actualOpts) throws Exception {
        assertPrepareOptionEquals(actualOpts);
        assertOptionEquals("auto-perform", actualOpts);
        assertOptionEquals("auto-commit", actualOpts);
        assertOptionEquals("parallel", sampleParallel, actualOpts);
    }
    public void assertOptionEquals(String expectedName, Options actual)
            throws Exception {
        OptionSet set = actual.getOptions();
        assertTrue(set.has(expectedName));
        assertFalse(set.hasArgument(expectedName));
    }

    public void assertOptionEquals(String expectedName, String expectedArg,
            Options actual) throws Exception {
        assertOptionWithRequiredArgEquals(expectedName, actual);
        assertEquals(expectedArg, actual.getOptions().valueOf(expectedName));
    }

    public void assertOptionEquals(String expectedName, String[] expectedArgs,
            Options actual) throws Exception {
        assertOptionWithRequiredArgEquals(expectedName, actual);
        @SuppressWarnings("unchecked")
        List<String> args = (List<String>) actual.getOptions().valuesOf(expectedName);
        assertEquals(expectedArgs.length, args.size());
        for (int i = 0; i < expectedArgs.length; i++) {
            assertEquals(expectedArgs[i], args.get(i));
        }
    }

    public void assertOptionWithRequiredArgEquals(String expectedName,
            Options actual) throws Exception {
        OptionSet set = actual.getOptions();
        assertTrue(set.has(expectedName));
        assertTrue(set.hasArgument(expectedName));
    }

}
