package com.treasure_data.td_import.prepare;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.IOException;

import org.junit.Ignore;

import com.treasure_data.td_import.prepare.PrepareConfiguration;
import com.treasure_data.td_import.prepare.Task;
import com.treasure_data.td_import.source.LocalFileSource;

@Ignore
public class PrepareProcessorTestUtil {

    public static Task createTask(int i, int numRows) {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("time,user,age\n");
        for (int j = 0; j < numRows; j++) {
            sbuf.append(String.format("1370416181,muga%d,%d\n", i, i));
        }

        Task t = new Task(new LocalFileSource("file" + i));
        t.isTest = true;
        t.testBinary = sbuf.toString().getBytes();
        return t;
    }

    public static Task createErrorTask(int i)
            throws Exception {
        Task t = new Task(new LocalFileSource("file" + i));
        t = spy(t);
        doThrow(new IOException("dummy")).when(t).createInputStream(
                any(PrepareConfiguration.CompressionType.class));
        return t;
    }
}
