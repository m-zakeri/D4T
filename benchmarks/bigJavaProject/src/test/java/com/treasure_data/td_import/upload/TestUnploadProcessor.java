package com.treasure_data.td_import.upload;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.util.Properties;
import java.util.Random;

import com.treasuredata.client.TDClient;
import com.treasuredata.client.TDClientBuilder;
import com.treasuredata.client.TDClientException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.treasure_data.td_import.Options;
import com.treasure_data.td_import.source.LocalFileSource;

public class TestUnploadProcessor {

    @Test @Ignore
    public void test01() throws Exception {
        Properties props = System.getProperties();
        props.load(this.getClass().getClassLoader().getResourceAsStream("treasure-data.properties"));
        TDClientBuilder builder = TDClient.newBuilder();
        builder.setProperties(props);
        TDClient client = builder.build();

        UploadConfiguration conf = new UploadConfiguration();
        conf.configure(props, options);
        UploadProcessor proc = new UploadProcessor(client, conf);

        byte[] bytes = "muga".getBytes();
        String sessName = "mugasess";
        String fileName = "file01";

        UploadTask task = new UploadTask(sessName, new LocalFileSource(fileName));
        task = spy(task);
        task.isTest = true;
        task.testBinary = bytes;
        proc.execute(task);
    }

    private Properties props;
    protected Options options;
    private UploadConfiguration conf;
    private UploadProcessor proc;

    private UploadTask task;
    private TaskResult err;

    Random rand = new Random(new Random().nextInt());
    private int numTasks;

    @Before
    public void createResources() throws Exception {
        props = System.getProperties();

        options = new Options();
        options.initUploadOptionParser(props);
        options.setOptions(new String[0]);

        // create upload config
        conf = new UploadConfiguration();
        conf.configure(props, options);

        // create upload processor
        proc = new UploadProcessor(null, conf);

        numTasks = rand.nextInt(30) + 1;
    }

    @After
    public void destroyResources() throws Exception {
    }

    @Test
    public void returnNonErrorWhenExecuteMethodWorksNormally() throws Exception {
        // configure mock
        proc = spy(proc);
        doNothing().when(proc).executeUpload(any(UploadTask.class));

        // test
        for (int i = 0; i < numTasks; i++) {
            task = UploadProcessorTestUtil.createTask(i);
            UploadProcessorTestUtil.executeTaskNormally(proc, task, proc.execute(task));
        }
    }

    @Test
    public void returnIOErrorWhenExecuteMethodThrowsIOError() throws Exception {
        // configure mock
        proc = spy(proc);
        doThrow(new TDClientException(TDClientException.ErrorType.CLIENT_ERROR, "dummy")).when(proc).executeUpload(any(UploadTask.class));

        // test
        for (int i = 0; i < numTasks; i++) {
            task = UploadProcessorTestUtil.createTask(i);
            UploadProcessorTestUtil.failTask(proc, task, proc.execute(task));
        }
    }

    @Test
    public void returnIOErrorWhenExecuteMethodThrowsClientError() throws Exception {
        // configure mock
        proc = spy(proc);
        doThrow(new TDClientException(TDClientException.ErrorType.CLIENT_ERROR, "dummy")).when(proc).executeUpload(any(UploadTask.class));

        // test
        int count = 1;
        for (int i = 0; i < count; i++) {
            task = UploadProcessorTestUtil.createTask(i);
            UploadProcessorTestUtil.failTask(proc, task, proc.execute(task));
        }
    }
}
