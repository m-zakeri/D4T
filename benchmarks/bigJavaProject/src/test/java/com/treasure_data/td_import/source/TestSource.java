package com.treasure_data.td_import.source;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class TestSource {

    static String getFileName(String... names) {
        StringBuilder sbuf = new StringBuilder();
        for (int i = 0; i < names.length; i++) {
            if (i != 0) {
                sbuf.append(File.separatorChar);
            }
            sbuf.append(names[i]);
        }
        return sbuf.toString();
    }

    @Test
    public void testLocalFileSource() throws Exception {
        String fileName = getFileName("aaa", "kb", "kc.gz"); // "aaa/kb/kc.gz" or "aaa\kb\kc.gz"
        LocalFileSource source = new LocalFileSource(fileName);
        String inName = source.getPath();
        assertEquals(fileName, inName);
        int lastSepIndex = inName.lastIndexOf(source.getSeparatorChar());
        String outputFilePrefix = inName.substring(lastSepIndex + 1, inName.length()).replace('.', '_');
        assertEquals("kc_gz", outputFilePrefix);
    }

    @Test
    public void testS3Source() throws Exception {
        S3Source source = new S3Source(null, "s3://aaa/kb/kc.gz", "aaa", "kb/kc.gz", 10);
        String inName = source.getPath();
        assertEquals("s3://aaa/kb/kc.gz", inName);
        int lastSepIndex = inName.lastIndexOf(source.getSeparatorChar());
        String outputFilePrefix = inName.substring(lastSepIndex + 1, inName.length()).replace('.', '_');
        assertEquals("kc_gz", outputFilePrefix);
    }
}
