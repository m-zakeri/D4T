package com.treasure_data.td_import.integration;

import java.io.IOException;
import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.Ignore;

@Ignore
public class JSONFileGenerator extends FileGenerator {
    private static final String LF = "\n";

    public JSONFileGenerator(String fileName, String[] header)
            throws IOException {
        super(fileName, header);
    }

    public void writeHeader() throws IOException {
        // doesn't have header
    }

    public void write(Map<String, Object> map) throws IOException {
        out.write(JSONObject.toJSONString(map).getBytes());
        out.write(LF.getBytes());
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}
