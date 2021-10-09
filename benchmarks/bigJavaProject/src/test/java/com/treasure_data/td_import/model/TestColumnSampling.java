package com.treasure_data.td_import.model;

import java.util.Random;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.treasure_data.td_import.model.ColumnSampling;
import com.treasure_data.td_import.model.ColumnType;

public class TestColumnSampling {

    protected Random rand = new Random(new Random().nextInt());

    protected int numRows;
    protected ColumnType expected;
    protected ColumnSampling sampling;

    @Before
    public void createResources() throws Exception {
        numRows = rand.nextInt(100) + 1;
        sampling = new ColumnSampling(numRows);
    }

    @After
    public void destroyResources() throws Exception {
    }

    @Test
    public void workStringValuesNormally() throws Exception {
        expected = ColumnType.STRING;
        workValuesNormally(expected.getName());
    }

    @Test
    public void workLongValuesNormally() throws Exception {
        expected = ColumnType.LONG;
        workValuesNormally(expected.getName());
    }

    @Test
    public void workDoubleValuesNormally() throws Exception {
        expected = ColumnType.DOUBLE;
        workValuesNormally(expected.getName());
    }

    @Test
    public void workBigIntValuesNormally() throws Exception {
        String[] values = getValues(numRows, ColumnType.BIGINT.getName());
        parse(values);
        String actual = sampling.getRank().getName();
        Assert.assertEquals("string", actual);
    }

    private void workValuesNormally(String expected) throws Exception {
        String[] values = getValues(numRows, expected);
        parse(values);
        String actual = sampling.getRank().getName();
        Assert.assertEquals(expected, actual);
    }

    private void parse(String[] values) {
        for (int i = 0; i < numRows; i++) {
            sampling.parse(values[i]);
        }
    }
    private String[] getValues(int num, String typeName) {
        if (typeName.equals("string")) {
            return getStringValues(num);
        } else if (typeName.equals("long")) {
            return getLongValues(num);
        } else if (typeName.equals("double")) {
            return getDoubleValues(num);
        } else if (typeName.equals("bigint")) {
            return getBigIntValues(num);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private String[] getStringValues(int num) {
        String[] values = new String[num];
        for (int i = 0; i < values.length; i++) {
            values[i] = "muga:" + rand.nextInt();
        }
        return values;
    }

    private String[] getLongValues(int num) {
        String[] values = new String[num];
        for (int i = 0; i < values.length; i++) {
            values[i] = "" + rand.nextLong();
        }
        return values;
    }

    private String[] getDoubleValues(int num) {
        String[] values = new String[num];
        for (int i = 0; i < values.length; i++) {
            values[i] = "" + rand.nextDouble();
        }
        return values;
    }

    private String[] getBigIntValues(int num) {
        String[] values = new String[num];
        for (int i = 0; i < values.length; i++) {
            values[i] = "1111111222222222222222222222211111111";
        }
        return values;
    }
}
