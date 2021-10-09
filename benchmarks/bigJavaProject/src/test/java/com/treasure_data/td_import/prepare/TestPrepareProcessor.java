package com.treasure_data.td_import.prepare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.treasure_data.td_import.Options;
import com.treasure_data.td_import.Configuration;
import com.treasure_data.td_import.model.ColumnType;
import com.treasure_data.td_import.prepare.CSVPrepareConfiguration;
import com.treasure_data.td_import.prepare.PrepareConfiguration;
import com.treasure_data.td_import.prepare.PreparePartsException;
import com.treasure_data.td_import.prepare.PrepareProcessor;
import com.treasure_data.td_import.prepare.Task;
import com.treasure_data.td_import.prepare.TaskResult;
import com.treasure_data.td_import.source.LocalFileSource;
import com.treasure_data.td_import.source.Source;

public class TestPrepareProcessor {

    @Test @Ignore
    public void test01() throws Exception {
        Properties props = System.getProperties();
        props.load(this.getClass().getClassLoader().getResourceAsStream("treasure-data.properties"));

        CSVPrepareConfiguration conf = new CSVPrepareConfiguration();
        conf = spy(conf);
        doReturn(PrepareConfiguration.CompressionType.NONE).when(conf).checkCompressionType(any(Source.class));
        doReturn(PrepareConfiguration.CompressionType.NONE).when(conf).getCompressionType();
        conf.configure(props, options);
        PrepareProcessor proc = new PrepareProcessor(conf);

        String csvtext = "time,user,age\n" + "1370416181,muga,10\n";
        String fileName = "file01";

        Task task = new Task(new LocalFileSource(fileName));
        task.isTest = true;
        task.testBinary = csvtext.getBytes();

        TaskResult err = proc.execute(task);
    }


    private Properties props;
    protected Options options;
    private CSVPrepareConfiguration conf;
    private PrepareProcessor proc;

    private Task task;
    private TaskResult err;

    Random rand = new Random(new Random().nextInt());
    private int numTasks;
    private int numRows;

    @Before
    public void createResources() throws Exception {
        props = System.getProperties();

        options = new Options();
        options.initPrepareOptionParser(props);
        options.setOptions(new String[] {
                "--column-header"
        });

        // create prepare conf
        conf = new CSVPrepareConfiguration();
        conf.configure(props, options);
        conf = spy(conf);
        doReturn(PrepareConfiguration.CompressionType.NONE).when(conf).checkCompressionType(any(Source.class));
        doReturn(PrepareConfiguration.CompressionType.NONE).when(conf).getCompressionType();

        // create prepare processor
        proc = new PrepareProcessor(conf);

        numRows = rand.nextInt(100);
        numTasks = rand.nextInt(100);
    }

    @After
    public void destroyResources() throws Exception {
    }

    @Test
    public void dontGetErrorWhenExecuteMethodWorksNormally() throws Exception {
        proc = spy(proc);

        for (int i = 0; i < numTasks; i++) {
            task = PrepareProcessorTestUtil.createTask(i, numRows);
            err = proc.execute(task);
            assertEquals(task, err.task);
            assertEquals(null, err.error);
            assertEquals(numRows, err.convertedRows);
        }
    }

    @Test
    public void getIOErrorWhenExecuteMethodCannotFindCSVFile() throws Exception {
        proc = spy(proc);

        for (int i = 0; i < numTasks; i++) {
            task = PrepareProcessorTestUtil.createErrorTask(i);
            err = proc.execute(task);
            assertEquals(task, err.task);
            assertTrue(err.error instanceof PreparePartsException);
        }
    }
}
