package com.treasure_data.td_import.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.treasure_data.td_import.model.ColumnType;
import com.treasure_data.td_import.model.LongColumnValue;
import com.treasure_data.td_import.prepare.PreparePartsException;

public class TestLongColumnValue extends ColumnValueTestUtil<Long> {

    @Before
    public void createResources() throws Exception {
        super.createResources();
        columnValue = new LongColumnValue(conf, 0, ColumnType.LONG);
    }

    @After
    public void destroyResources() throws Exception {
        super.destroyResources();
    }

    @Override
    public void createExpecteds() {
        expecteds.add(Long.MAX_VALUE);
        expecteds.add(Long.MIN_VALUE);

        int numExec = rand.nextInt(10000);
        for (int i = 0; i < numExec; i++) {
            expecteds.add(rand.nextLong());
        }
    }

    @Test
    public void returnNormalValues() throws Exception {
        for (int i = 0; i < expecteds.size(); i++) {
            columnValue.parse("" + expecteds.get(i));
            assertColumnValueEquals(expecteds.get(i),
                    (LongColumnValue) columnValue);
        }
    }

    @Test
    public void returnNullWithEmptyAsNull() throws Exception {
        conf.setEmptyAsNull(true);
        columnValue.parse(null);
        assertEquals(0, ((LongColumnValue) columnValue).getLong());
        assertTrue(((LongColumnValue)columnValue).isEmptyString);
        assertFalse(((LongColumnValue) columnValue).isNullString);
    }

    void assertColumnValueEquals(long expected, LongColumnValue actual) {
        Assert.assertEquals(expected, actual.getLong());
    }

    @Override
    public void assertWrittenValueEquals(int index) {
        Assert.assertEquals(expecteds.get(index), (Long) writer.getRow().get(KEY));
    }

    @Override
    public void prepareMockForWriting() throws Exception {
        writer = spy(writer);
        doThrow(new PreparePartsException("")).when(writer).write(any(long.class));
    }
}
