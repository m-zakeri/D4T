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

public class TestJSONRecordReader {

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
                "--format",
                "json"
        });

        conf = PrepareConfiguration.Format.JSON.createPrepareConfiguration();
        conf.configure(props, options);

        writer = new FileWriterTestUtil(conf);
        reader = PrepareConfiguration.Format.JSON.createFileReader(conf, writer);

        Task task = new Task(new LocalFileSource("dummy.txt"));
        task = spy(task);
        task.isTest = true;
        task.testBinary = (
                "{\"k\":10, \"time\":1392238800}\n" +
                "{\"k\":null, \"time\":1392238900}\n" +
                "{\"k\":12, \"time\":null}\n"
                ).getBytes();
                

        reader.configure(task);
        writer.setActualColumnNames(reader.getActualColumnNames());
        writer.setColumnNames(reader.getColumnNames());
        writer.setColumnTypes(reader.getColumnTypes());
        writer.setSkipColumns(reader.getSkipColumns());
        writer.setTimeColumnValue(reader.getTimeColumnValue());

        try {
            reader.next();
            System.out.println(writer.getRow());
            writer.clear();

            reader.next();
            System.out.println(writer.getRow());
            writer.clear();

            reader.next();
            System.out.println(writer.getRow());
            writer.clear();
        } finally {
            reader.close();
        }

    }
}
