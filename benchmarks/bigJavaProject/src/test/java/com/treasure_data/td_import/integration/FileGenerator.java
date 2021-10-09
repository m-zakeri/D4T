package com.treasure_data.td_import.integration;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.junit.Ignore;

@Ignore
public class FileGenerator implements Closeable {
    protected OutputStream out;
    protected String[] header;

    public FileGenerator(String fileName, String[] header) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }

        this.out = new BufferedOutputStream(new FileOutputStream(file));
        this.header = header;    
    }

    public void writeHeader() throws IOException {
    }

    public void write(Map<String, Object> row) throws IOException {
    }

    public void close() throws IOException {
        if (out != null) {
            out.flush();
        }

        if (out != null) {
            out.close();
        }
    }
}
