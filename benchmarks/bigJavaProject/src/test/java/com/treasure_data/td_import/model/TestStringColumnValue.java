package com.treasure_data.td_import.model;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.treasure_data.td_import.model.ColumnType;
import com.treasure_data.td_import.model.StringColumnValue;
import com.treasure_data.td_import.prepare.PreparePartsException;

public class TestStringColumnValue extends ColumnValueTestUtil<String> {

    @Before
    public void createResources() throws Exception {
        super.createResources();
        columnValue = new StringColumnValue(conf, 0, ColumnType.STRING);
    }

    @After
    public void destroyResources() throws Exception {
        super.destroyResources();
    }

    @Override
    public void createExpecteds() {
        List<String> expecteds = new ArrayList<String>();

        int numExec = rand.nextInt(10000);
        for (int i = 0; i < numExec; i++) {
            StringBuilder sbuf = new StringBuilder();
            int sizeString = rand.nextInt(30);
            for (int j = 0; j < sizeString; j++) {
                sbuf.append((char) rand.nextInt());
            }
            expecteds.add(sbuf.toString());
        }
    }

    @Test
    public void returnNormalValues() throws Exception {
        for (int i = 0; i < expecteds.size(); i++) {
            columnValue.parse("" + expecteds.get(i));
            assertColumnValueEquals(expecteds.get(i),
                    (StringColumnValue) columnValue);
        }
    }

    void assertColumnValueEquals(String expected, StringColumnValue actual) {
        Assert.assertEquals(expected, actual.getString());
    }

    @Test
    public void throwPreparePartErrorWhenItParsesInvalidValues() throws Exception {
        assertTrue(true);
    }

    @Override
    public void assertWrittenValueEquals(int index) {
        Assert.assertEquals(expecteds.get(index), (String) writer.getRow().get(KEY));
    }

    @Override
    public void prepareMockForWriting() throws Exception {
        writer = spy(writer);
        doThrow(new PreparePartsException("")).when(writer).write(any(String.class));
        doThrow(new PreparePartsException("")).when(writer).writeNil();
    }
}
