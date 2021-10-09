package com.treasure_data.td_import.integration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.junit.Ignore;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.value.Value;
import org.msgpack.value.ValueFactory;

@Ignore
public class TrainingDataFileGenerator extends FileGenerator {

    protected MessagePacker packer;

    protected List<Object> kvs;

    public TrainingDataFileGenerator(String fileName, String[] header) throws IOException {
        super(fileName, header);

        out = new GZIPOutputStream(this.out);
        packer = MessagePack.newDefaultPacker(out);
        this.kvs = new ArrayList<Object>();
    }

    @Override
    public void writeHeader() throws IOException {
        // ignore
    }

    public void write(Map<String, Object> map) throws IOException {
        packer.packMapHeader(map.size());
        ValueFactory.MapBuilder b = ValueFactory.newMapBuilder();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            b.put(ValueFactory.newString(e.getKey()), (Value) e.getValue());
        }
        packer.packValue(b.build());
    }

    @Override
    public void close() throws IOException {
        if (packer != null) {
            packer.flush();
        }

        if (packer != null) {
            packer.close();
        }
    }
}
