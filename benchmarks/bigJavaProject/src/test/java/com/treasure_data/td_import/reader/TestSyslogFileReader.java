package com.treasure_data.td_import.reader;

import static org.mockito.Mockito.spy;

import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;

import com.treasure_data.td_import.Options;
import com.treasure_data.td_import.Configuration;
import com.treasure_data.td_import.prepare.PrepareConfiguration;
import com.treasure_data.td_import.prepare.Task;
import com.treasure_data.td_import.reader.RecordReader;
import com.treasure_data.td_import.source.LocalFileSource;
import com.treasure_data.td_import.writer.FileWriterTestUtil;

public class TestSyslogFileReader {

    protected Properties props;
    protected Options options;
    protected PrepareConfiguration conf;
    protected FileWriterTestUtil writer;
    protected RecordReader reader;

    @Test
    public void sample() throws Exception {
        props = new Properties();
        props.setProperty(Configuration.BI_PREPARE_PARTS_SAMPLE_ROWSIZE, "1");

        options = new Options();
        options.initPrepareOptionParser(props);
        options.setOptions(new String[] {
                "--column-header",
        });

        conf = PrepareConfiguration.Format.SYSLOG.createPrepareConfiguration();
        conf.configure(props, options);

        writer = new FileWriterTestUtil(conf);
        reader = PrepareConfiguration.Format.SYSLOG.createFileReader(conf, writer);

        Task task = new Task(new LocalFileSource("dummy.txt"));
        task = spy(task);
        task.isTest = true;
        task.testBinary =
                ("Jul 01 00:19:00 muga88 muga88(muga88)[1528965344]: muga88\n"
                        + "Jul 27 09:49:38 itbsv1 su(pam_unix)[8061]: session opened for user root by root(uid=0)\n"
                        + "Jul 27 09:49:38 itbsv1 su(pam_unix)[8061]: session opened for user root by root(uid=0)\n").getBytes();

        reader.configure(task);
        writer.setActualColumnNames(reader.getActualColumnNames());
        writer.setColumnNames(reader.getColumnNames());
        writer.setColumnTypes(reader.getColumnTypes());
        writer.setSkipColumns(reader.getSkipColumns());
        writer.setTimeColumnValue(reader.getTimeColumnValue());

        try {
            reader.next();
            writer.clear();

            reader.next();
            writer.clear();

            reader.next();
            writer.clear();
        } finally {
            reader.close();
        }

    }
}
