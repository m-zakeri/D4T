package com.treasure_data.td_import.integration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;

@Ignore
public class CSVFileGenerator extends FileGenerator {
    protected static final String LF = "\n";
    protected static final String COMMA = ",";

    protected Map<String, Integer> headerMap;

    protected Object[] row;

    public CSVFileGenerator(String fileName, String[] header)
            throws IOException {
        super(fileName, header);

        headerMap = new HashMap<String, Integer>(header.length);
        for (int i = 0; i < header.length; i++) {
            headerMap.put(header[i], i);
        }
        row = new Object[header.length];
    }

    @Override
    public void writeHeader() throws IOException {
        for (int i = 0; i < header.length; i++) {
            out.write(header[i].getBytes());
            if (i != header.length - 1) {
                out.write(getDelimiter().getBytes());
            }
        }
        out.write(getNewLine().getBytes());
    }

    @Override
    public void write(Map<String, Object> map) throws IOException {
        for (Map.Entry<String, Object> e : map.entrySet()) {
            int i = headerMap.get(e.getKey());
            row[i] = e.getValue();
        }

        for (int i = 0; i < header.length; i++) {
            out.write(row[i].toString().getBytes());
            if (i != header.length - 1) {
                out.write(getDelimiter().getBytes());
            }
        }
        out.write(getNewLine().getBytes());
    }

    protected String getDelimiter() {
        return COMMA;
    }

    protected String getNewLine() {
        return LF;
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}
