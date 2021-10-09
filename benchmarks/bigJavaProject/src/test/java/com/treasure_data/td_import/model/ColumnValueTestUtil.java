package com.treasure_data.td_import.model;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.treasure_data.td_import.Options;
import com.treasure_data.td_import.model.ColumnValue;
import com.treasure_data.td_import.prepare.PrepareConfiguration;
import com.treasure_data.td_import.prepare.PreparePartsException;
import com.treasure_data.td_import.writer.FileWriterTestUtil;

@Ignore
public class ColumnValueTestUtil<T> {

    protected Properties props;
    protected Options options;
    protected PrepareConfiguration conf;

    protected ColumnValue columnValue;

    protected List<T> expecteds = new ArrayList<T>();
    protected Random rand = new Random(new Random().nextInt());

    protected FileWriterTestUtil writer;
    protected static final String KEY = "key";

    @Before
    public void createResources() throws Exception {
        props = System.getProperties();

        options = new Options();
        options.initPrepareOptionParser(props);
        options.setOptions(new String[0]);

        conf = new PrepareConfiguration();
        conf.configure(props, options);

        writer = new FileWriterTestUtil(conf);
        writer.configure(null, null);

        createExpecteds();
    }

    @After
    public void destroyResources() throws Exception {
    }

    public void createExpecteds() {
        throw new UnsupportedOperationException();
    }

    public String invalidValue() {
        return "muga";
    }

    public void prepareMockForWriting() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Test
    public void returnNormalValues() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Test
    public void throwPreparePartErrorWhenItParsesInvalidValues() throws Exception {
        try {
            columnValue.parse(invalidValue());
            fail();
        } catch (Throwable t) {
            assertTrue(t instanceof PreparePartsException);
        }
    }

    @Test
    public void writeNormalValues() throws Exception {
        for (int i = 0; i < expecteds.size(); i++) {
            columnValue.parse("" + expecteds.get(i));

            writer.writeBeginRow(1);
            writer.write(KEY);
            columnValue.write(writer);
            writer.writeEndRow();
            writer.getRow().get("");
            assertWrittenValueEquals(i);
            writer.clear();
        }
        columnValue.write(writer);
    }

    public void assertWrittenValueEquals(int index) {
        throw new UnsupportedOperationException();
    }

    @Test
    public void throwPreparePartErrorWhenItWritesInvalidValues() throws Exception {
        prepareMockForWriting();

        try {
            columnValue.write(writer);
            fail();
        } catch (Throwable t) {
            assertTrue(t instanceof PreparePartsException);
        }
    }
}
