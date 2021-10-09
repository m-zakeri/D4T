package com.treasure_data.td_import.reader;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.spy;

import com.treasure_data.td_import.Options;
import com.treasure_data.td_import.Configuration;
import com.treasure_data.td_import.model.AliasTimeColumnValue;
import com.treasure_data.td_import.model.ColumnType;
import com.treasure_data.td_import.model.TimeColumnValue;
import com.treasure_data.td_import.model.TimeValueTimeColumnValue;
import com.treasure_data.td_import.prepare.CSVPrepareConfiguration;
import com.treasure_data.td_import.prepare.Task;
import com.treasure_data.td_import.reader.RecordReader;
import com.treasure_data.td_import.source.LocalFileSource;

public class TestCSVFileReader extends FileReaderTestUtil<CSVPrepareConfiguration> {

    private static final String LF = "\n";
    private static final String COMMA = ",";

    public static interface Context {
        public void createContext(TestCSVFileReader reader) throws Exception;

        public String generateCSVText(TestCSVFileReader reader);

        public void assertContextEquals(TestCSVFileReader reader);
    }

    public static class Context01 implements Context {
        public void createContext(TestCSVFileReader test)
                throws Exception {
            test.columnNames = new String[] { "name", "count", "time" };
            test.columnTypes = new ColumnType[] {
                    ColumnType.STRING, ColumnType.LONG, ColumnType.LONG };
        }

        public String generateCSVText(TestCSVFileReader test) {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append(test.columnNames[0]).append(COMMA);
            sbuf.append(test.columnNames[1]).append(COMMA);
            sbuf.append(test.columnNames[2]).append(LF);
            for (int i = 0; i < test.numLine; i++) {
                sbuf.append("muga" + i).append(COMMA);
                sbuf.append(i).append(COMMA);
                sbuf.append(test.baseTime + 60 * i).append(LF);
            }
            return sbuf.toString();
        }

        public void assertContextEquals(TestCSVFileReader test) {
            assertArrayEquals(test.columnNames, test.reader.getColumnNames());
            assertArrayEquals(test.columnTypes, test.reader.getColumnTypes());
            assertTrue(test.reader.getTimeColumnValue() instanceof TimeColumnValue);
            assertTrue(test.reader.getSkipColumns().isEmpty());
        }
    }

    public static class Context02 implements Context {
        public long getTimeValue() {
            return 12345;
        }

        public void createContext(TestCSVFileReader test)
                throws Exception {
            test.columnNames = new String[] { "name", "count" };
            test.columnTypes = new ColumnType[] {
                    ColumnType.STRING, ColumnType.LONG };
        }

        public String generateCSVText(TestCSVFileReader test) {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append(test.columnNames[0]).append(COMMA);
            sbuf.append(test.columnNames[1]).append(LF);
            for (int i = 0; i < test.numLine; i++) {
                sbuf.append("muga" + i).append(COMMA);
                sbuf.append(i).append(LF);
            }
            return sbuf.toString();
        }

        public void assertContextEquals(TestCSVFileReader test) {
            assertArrayEquals(test.columnNames, test.reader.getColumnNames());
            assertArrayEquals(test.columnTypes, test.reader.getColumnTypes());
            assertTrue(test.reader.getTimeColumnValue() instanceof TimeValueTimeColumnValue);
            assertEquals(getTimeValue(), ((TimeValueTimeColumnValue)test.reader.getTimeColumnValue()).getTimeValue());
            assertTrue(test.reader.getSkipColumns().isEmpty());
        }
    }

    public static class Context03 implements Context {
        public String getAliasTimeColumn() {
            return "date_code";
        }

        public void createContext(TestCSVFileReader test)
                throws Exception {
            test.columnNames = new String[] { "date_code", "name", "count" };
            test.columnTypes = new ColumnType[] {
                    ColumnType.LONG, ColumnType.STRING, ColumnType.LONG };
        }

        public String generateCSVText(TestCSVFileReader test) {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append(test.columnNames[0]).append(COMMA);
            sbuf.append(test.columnNames[1]).append(COMMA);
            sbuf.append(test.columnNames[2]).append(LF);
            for (int i = 0; i < test.numLine; i++) {
                sbuf.append(test.baseTime + 60 * i).append(COMMA);
                sbuf.append("muga" + i).append(COMMA);
                sbuf.append(i).append(LF);
            }
            return sbuf.toString();
        }

        public void assertContextEquals(TestCSVFileReader test) {
            assertArrayEquals(test.columnNames, test.reader.getColumnNames());
            assertArrayEquals(test.columnTypes, test.reader.getColumnTypes());
            assertTrue(test.reader.getTimeColumnValue() instanceof AliasTimeColumnValue);
            assertTrue(test.reader.getSkipColumns().isEmpty());
        }
    }

    public static class Context04 implements Context {
        public String getExcludeColumns() {
            return "foo,bar,baz";
        }

        public void createContext(TestCSVFileReader test)
                throws Exception {
            test.columnNames = new String[] { "foo", "name", "count", "bar", "time", "baz" };
            test.columnTypes = new ColumnType[] {
                    ColumnType.STRING, ColumnType.STRING, ColumnType.LONG,
                    ColumnType.STRING, ColumnType.LONG, ColumnType.STRING };
        }

        public String generateCSVText(TestCSVFileReader test) {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append(test.columnNames[0]).append(COMMA);
            sbuf.append(test.columnNames[1]).append(COMMA);
            sbuf.append(test.columnNames[2]).append(COMMA);
            sbuf.append(test.columnNames[3]).append(COMMA);
            sbuf.append(test.columnNames[4]).append(COMMA);
            sbuf.append(test.columnNames[5]).append(LF);
            for (int i = 0; i < test.numLine; i++) {
                sbuf.append("foo" + i).append(COMMA);
                sbuf.append("muga" + i).append(COMMA);
                sbuf.append(i).append(COMMA);
                sbuf.append("bar" + i).append(COMMA);
                sbuf.append(test.baseTime + 60 * i).append(COMMA); // time
                sbuf.append("baz" + i).append(LF);
            }
            return sbuf.toString();
        }

        public void assertContextEquals(TestCSVFileReader test) {
            assertArrayEquals(test.columnNames, test.reader.getColumnNames());
            assertArrayEquals(test.columnTypes, test.reader.getColumnTypes());
            assertTrue(test.reader.getTimeColumnValue() instanceof TimeColumnValue);
            assertEquals(3, test.reader.getSkipColumns().size());
        }
    }

    public static class Context05 implements Context {
        public String getOnlyColumns() {
            return "time,name,count";
        }

        public void createContext(TestCSVFileReader test)
                throws Exception {
            test.columnNames = new String[] { "foo", "name", "count", "bar", "time", "baz" };
            test.columnTypes = new ColumnType[] {
                    ColumnType.STRING, ColumnType.STRING, ColumnType.LONG,
                    ColumnType.STRING, ColumnType.LONG, ColumnType.STRING };
        }

        public String generateCSVText(TestCSVFileReader test) {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append(test.columnNames[0]).append(COMMA);
            sbuf.append(test.columnNames[1]).append(COMMA);
            sbuf.append(test.columnNames[2]).append(COMMA);
            sbuf.append(test.columnNames[3]).append(COMMA);
            sbuf.append(test.columnNames[4]).append(COMMA);
            sbuf.append(test.columnNames[5]).append(LF);
            for (int i = 0; i < test.numLine; i++) {
                sbuf.append("foo" + i).append(COMMA);
                sbuf.append("muga" + i).append(COMMA);
                sbuf.append(i).append(COMMA);
                sbuf.append("bar" + i).append(COMMA);
                sbuf.append(test.baseTime + 60 * i).append(COMMA); // time
                sbuf.append("baz" + i).append(LF);
            }
            return sbuf.toString();
        }

        public void assertContextEquals(TestCSVFileReader test) {
            assertArrayEquals(test.columnNames, test.reader.getColumnNames());
            assertArrayEquals(test.columnTypes, test.reader.getColumnTypes());
            assertTrue(test.reader.getTimeColumnValue() instanceof TimeColumnValue);
            assertEquals(3, test.reader.getSkipColumns().size());
        }
    }

    public static class Context06 implements Context {
        public String getSTRFTimeFormat() {
            return "%Y/%m/%d %k:%M:%S";
        }

        public void createContext(TestCSVFileReader test)
                throws Exception {
            test.columnNames = new String[] { "name", "count", "time" };
            test.columnTypes = new ColumnType[] {
                    ColumnType.STRING, ColumnType.LONG, ColumnType.STRING };
        }

        public String generateCSVText(TestCSVFileReader test) {
            StringBuilder sbuf = new StringBuilder();
            sbuf.append(test.columnNames[0]).append(COMMA);
            sbuf.append(test.columnNames[1]).append(COMMA);
            sbuf.append(test.columnNames[2]).append(LF);
            for (int i = 0; i < test.numLine; i++) {
                sbuf.append("muga" + i).append(COMMA);
                sbuf.append(i).append(COMMA);
                sbuf.append("2013/05/05 14:" + ((i % 50) + 10) + ":00").append(LF);
            }
            return sbuf.toString();
        }

        public void assertContextEquals(TestCSVFileReader test) {
            assertArrayEquals(test.columnNames, test.reader.getColumnNames());
            assertArrayEquals(test.columnTypes, test.reader.getColumnTypes());
            assertTrue(test.reader.getTimeColumnValue() instanceof TimeColumnValue);
            assertTrue(test.reader.getSkipColumns().isEmpty());
        }
    }

    protected String fileName = "./file.csv";
    protected int numLine;

    @Before
    public void createResources() throws Exception {
        super.createResources();
    }

    @Override
    public void createProperties() throws Exception {
        super.createProperties();

        numLine = rand.nextInt(10) + 1;

        props.setProperty(Configuration.BI_PREPARE_PARTS_SAMPLE_ROWSIZE, "" + numLine);
    }

    @Override
    public void createBulkImportOptions() throws Exception {
        super.createBulkImportOptions();
        options.setOptions(new String[] {
                "--format",
                "csv",
                "--column-header",
        });
    }

    @Override
    public void createPrepareConfiguration() throws Exception {
        conf = new CSVPrepareConfiguration();
        conf.configure(props, options);
    }

    @Override
    public void createFileReader() throws Exception {
        super.createFileReader();
        reader = (RecordReader<CSVPrepareConfiguration>)
                conf.getFormat().createFileReader(conf, writer);
    }

    @After
    public void destroyResources() throws Exception {
        super.createResources();
    }

    @Test
    public void checkContextWhenReaderConfiguration01() throws Exception {
        Context01 context = new Context01();
        checkContextWhenReaderConfiguration(context);
    }

    @Test
    public void checkContextWhenReaderConfigurationWithTimeValue() throws Exception {
        Context02 context = new Context02();

        // override system properties:-(
        options = new Options();
        options.initPrepareOptionParser(props);
        options.setOptions(new String[] {
                "--format",
                "csv",
                "--column-header",
                "--time-value",
                "" + context.getTimeValue(),
        });
        createPrepareConfiguration();
        createFileWriter();
        createFileReader();

        checkContextWhenReaderConfiguration(context);
    }

    @Test
    public void checkContextWhenReaderConfigurationWithAliasTimeColumn() throws Exception {
        Context03 context = new Context03();

        // override system properties:-(
        options = new Options();
        options.initPrepareOptionParser(props);
        options.setOptions(new String[] {
                "--format",
                "csv",
                "--column-header",
                "--time-column",
                context.getAliasTimeColumn(),
        });
        createPrepareConfiguration();
        createFileWriter();
        createFileReader();

        checkContextWhenReaderConfiguration(context);
    }

    @Test
    public void checkContextWhenReaderConfigurationWithExcludeColumns() throws Exception {
        Context04 context = new Context04();

        // override system properties:-(
        options = new Options();
        options.initPrepareOptionParser(props);
        options.setOptions(new String[] {
                "--format",
                "csv",
                "--column-header",
                "--exclude-columns",
                context.getExcludeColumns(),
        });
        createPrepareConfiguration();
        createFileWriter();
        createFileReader();

        checkContextWhenReaderConfiguration(context);
    }

    @Test
    public void checkContextWhenReaderConfigurationWithOnlyColumns() throws Exception {
        Context05 context = new Context05();

        // override system properties:-(
        options = new Options();
        options.initPrepareOptionParser(props);
        options.setOptions(new String[] {
                "--format",
                "csv",
                "--column-header",
                "--only-columns",
                context.getOnlyColumns(),
        });
        createPrepareConfiguration();
        createFileWriter();
        createFileReader();

        checkContextWhenReaderConfiguration(context);
    }

    @Test
    public void checkContextWhenReaderConfigurationWithTimeFormat() throws Exception {
        Context06 context = new Context06();

        // override system properties:-(
        options = new Options();
        options.initPrepareOptionParser(props);
        options.setOptions(new String[] {
                "--format",
                "csv",
                "--column-header",
                "--time-format",
                context.getSTRFTimeFormat(),
        });

        createPrepareConfiguration();
        createFileWriter();
        createFileReader();

        checkContextWhenReaderConfiguration(context);
    }

    private void checkContextWhenReaderConfiguration(Context context) throws Exception {
        // create context
        context.createContext(this);

        // create task
        task = new Task(new LocalFileSource(fileName));
        task = spy(task);
        task.isTest = true;
        task.testBinary = context.generateCSVText(this).getBytes();

        // call configure(task)
        reader.configure(task);
        context.assertContextEquals(this);
    }
}
