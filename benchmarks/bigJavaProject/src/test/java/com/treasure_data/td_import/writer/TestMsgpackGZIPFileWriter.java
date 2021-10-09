package com.treasure_data.td_import.writer;

import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.treasure_data.td_import.Options;
import com.treasure_data.td_import.Configuration;
import com.treasure_data.td_import.prepare.PrepareConfiguration;
import com.treasure_data.td_import.writer.RecordWriter;

public class TestMsgpackGZIPFileWriter {

    protected Properties props;
    protected Options options;
    protected PrepareConfiguration conf;
    protected RecordWriter writer;

    @Before
    public void createResources() throws Exception {
        // create system properties
        props = System.getProperties();
        props.setProperty(Configuration.BI_PREPARE_PARTS_OUTPUTFORMAT,
                Configuration.BI_PREPARE_PARTS_OUTPUTFORMAT_DEFAULTVALUE); // msgpackgz

        // create options
        options = new Options();
        options.initPrepareOptionParser(props);
        options.setOptions(new String[0]);

        // create configuration
        conf = new PrepareConfiguration();
        conf.configure(props, options);

        // create writer
        writer = conf.getOutputFormat().createFileWriter(conf);

    }

    @After
    public void destroyResources() throws Exception {
    }

    @Test
    public void dummy() throws Exception {
        assertTrue(true);
    }
}
