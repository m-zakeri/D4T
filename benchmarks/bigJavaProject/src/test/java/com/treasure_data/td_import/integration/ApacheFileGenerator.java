package com.treasure_data.td_import.integration;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.junit.Ignore;
import org.msgpack.value.Value;
import org.msgpack.value.ValueFactory;

@Ignore
public class ApacheFileGenerator extends FileGenerator {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");

    static Value HOST_VALUE = ValueFactory.newString("host");
    static Value USER_VALUE = ValueFactory.newString("user");
    static Value METHOD_VALUE = ValueFactory.newString("method");
    static Value PATH_VALUE = ValueFactory.newString("path");
    static Value CODE_VALUE = ValueFactory.newString("code");
    static Value SIZE_VALUE = ValueFactory.newString("size");

    protected static final String SPACE = " ";
    protected static final String LF = "\n";

    // "host", "user", "time", "method", "path", "code", "size", "referer", "agent"

    public ApacheFileGenerator(String fileName, String[] header)
            throws IOException {
        super(fileName, header);
    }

    @Override
    public void writeHeader() throws IOException {
        // non header
    }

    @Override
    public void write(Map<String, Object> map) throws IOException {
        StringBuilder sbuf = new StringBuilder();
        //127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] "GET /apache_pb.gif HTTP/1.0" 200 2326
        sbuf.append(map.get("string_value")) // host
            .append(SPACE)
            .append("-")
            .append(SPACE)
            .append(map.get("string_value")) // user
            .append(SPACE)
            .append("[")
            .append(dateFormat.format(new Date(((long) ((Long) map.get("time"))) * 1000)))
            .append("]")
            .append(SPACE)
            .append("\"")
            .append(map.get("string_value")) // method
            .append(SPACE)
            .append(map.get("string_value")) // path
            .append(SPACE)
            .append("xxxx") // protocol
            .append("\"")
            .append(SPACE)
            .append(map.get("int_value")) // code
            .append(SPACE)
            .append(map.get("int_value")); // size
        
        out.write(sbuf.toString().getBytes());
        out.write(LF.getBytes());
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}
