package com.treasure_data.td_import.upload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.IOException;

import org.junit.Ignore;

import com.treasure_data.td_import.source.LocalFileSource;
import com.treasure_data.td_import.upload.TaskResult;
import com.treasure_data.td_import.upload.UploadProcessor;
import com.treasure_data.td_import.upload.UploadTask;

@Ignore
public class UploadProcessorTestUtil {

    public static UploadTask createTask(int i) {
        LocalFileSource s = new LocalFileSource("file" + i);
        s = spy(s);
        doReturn(1L).when(s).getSize();
        return new UploadTask("sess" + i, s);
    }

    public static void executeTaskNormally(UploadProcessor proc,
            UploadTask task, TaskResult err) {
        assertEquals(task, err.task);
        assertEquals(null, err.error);
    }

    public static void failTask(UploadProcessor proc,
            UploadTask task, TaskResult err) {
        assertEquals(task, err.task);
        assertTrue(err.error instanceof IOException);
    }
}
