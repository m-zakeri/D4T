package com.treasure_data.td_import.integration;

import java.io.IOException;
import java.util.Map;

import org.junit.Ignore;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.value.Value;
import org.msgpack.value.ValueFactory;

@Ignore
public class MessagePackFileGenerator extends FileGenerator {
    private MessagePacker packer;

    public MessagePackFileGenerator(String fileName, String[] header)
            throws IOException {
        super(fileName, header);
        packer = MessagePack.newDefaultPacker(out);
    }

    public void writeHeader() throws IOException {
        // doesn't have header
    }

    public void write(Map<String, Object> map) throws IOException {
        ValueFactory.MapBuilder b = ValueFactory.newMapBuilder();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            b.put(ValueFactory.newString(e.getKey()), (Value) e.getValue());
        }
        packer.packValue(b.build());
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}
