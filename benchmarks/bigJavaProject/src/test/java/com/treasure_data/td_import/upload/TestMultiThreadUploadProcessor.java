package com.treasure_data.td_import.upload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import com.treasuredata.client.TDClientException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.treasure_data.td_import.Options;
import com.treasure_data.td_import.OptionsTestUtil;
import com.treasure_data.td_import.source.LocalFileSource;

public class TestMultiThreadUploadProcessor {

    @Test @Ignore
    public void test01() throws Exception {
        Properties props = System.getProperties();
        props.load(this.getClass().getClassLoader().getResourceAsStream("treasure-data.properties"));

        UploadConfiguration conf = new UploadConfiguration();
        conf.configure(props, options);
        MultiThreadUploadProcessor proc = new MultiThreadUploadProcessor(conf);
        proc.registerWorkers();
        proc.startWorkers();

        for (int i = 0; i < 10; i++) {
            byte[] bytes = ("muga" + i).getBytes();
            String sessName = "mugasess";
            String fileName = "file" + i;

            UploadTask task = new UploadTask(sessName, new LocalFileSource(fileName));
            task = spy(task);
            task.isTest = true;
            task.testBinary = bytes;
            MultiThreadUploadProcessor.addTask(task);
        }

        MultiThreadUploadProcessor.addFinishTask(conf);
        proc.joinWorkers();
    }

    private Properties props;
    protected Options options;
    private UploadConfiguration conf;
    private MultiThreadUploadProcessor proc;

    Random rand = new Random(new Random().nextInt());

    private int numTasks;
    private int numWorkers;

    @Before
    public void createResources() throws Exception {
        numWorkers = (rand.nextInt(10) % 8) + 1;
        numTasks = rand.nextInt(10) + 1;

        props = System.getProperties();

        // create options
        options = OptionsTestUtil.createUploadOptions(props,
                new String[] { "--parallel", "" + numWorkers });

        // create upload config
        conf = new UploadConfiguration();
        conf.configure(props, options);

        // create multi-thread upload processor
        proc = new MultiThreadUploadProcessor(conf);
    }

    @After
    public void destroyResources() throws Exception {
        MultiThreadUploadProcessor.clearTasks();
    }

    @Test
    public void dontGetErrorsWhenWorkersWorkNormally() throws Exception {
        for (int i = 0; i < numWorkers; i++) {
            UploadProcessor child = spy(new UploadProcessor(null, conf));
            doNothing().when(child).executeUpload(any(UploadTask.class));
            proc.addWorker(new MultiThreadUploadProcessor.Worker(proc, child));
        }
        proc.startWorkers();

        for (int i = 0; i < numTasks; i++) {
            MultiThreadUploadProcessor.addTask(UploadProcessorTestUtil.createTask(i));
        }

        MultiThreadUploadProcessor.addFinishTask(conf);
        proc.joinWorkers();

        assertEquals(numTasks, proc.getTaskResults().size());
    }

    @Test
    public void getErrorsWhenWorkersThrowIOError() throws Exception {
        for (int i = 0; i < numWorkers; i++) {
            UploadProcessor child = spy(new UploadProcessor(null, conf));
            doThrow(new TDClientException(TDClientException.ErrorType.CLIENT_ERROR, "dummy")).when(child).executeUpload(any(UploadTask.class));
            proc.addWorker(new MultiThreadUploadProcessor.Worker(proc, child));
        }
        proc.startWorkers();

        for (int i = 0; i < numTasks; i++) {
            MultiThreadUploadProcessor.addTask(UploadProcessorTestUtil.createTask(i));
        }

        MultiThreadUploadProcessor.addFinishTask(conf);
        proc.joinWorkers();

        assertEquals(numTasks, proc.getTaskResults().size());
        for (TaskResult err : proc.getTaskResults()) {
            assertTrue(err.error instanceof IOException);
        }
    }

    @Test
    public void getErrorsWhenWorkersThrowClientError() throws Exception {
        for (int i = 0; i < numWorkers; i++) {
            UploadProcessor child = spy(new UploadProcessor(null, conf));
            doThrow(new TDClientException(TDClientException.ErrorType.SERVER_ERROR, "dummy")).when(child).executeUpload(any(UploadTask.class));
            proc.addWorker(new MultiThreadUploadProcessor.Worker(proc, child));
        }
        proc.startWorkers();

        for (int i = 0; i < numTasks; i++) {
            MultiThreadUploadProcessor.addTask(UploadProcessorTestUtil.createTask(i));
        }

        MultiThreadUploadProcessor.addFinishTask(conf);
        proc.joinWorkers();

        assertEquals(numTasks, proc.getTaskResults().size());
        for (TaskResult err : proc.getTaskResults()) {
            assertTrue(err.error instanceof IOException);
        }
    }
}
