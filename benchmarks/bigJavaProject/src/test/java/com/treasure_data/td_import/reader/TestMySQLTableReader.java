package com.treasure_data.td_import.reader;

import com.treasure_data.td_import.model.ColumnType;
import org.junit.Test;

import java.sql.Types;

import static com.treasure_data.td_import.reader.MySQLTableReader.toColumnType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestMySQLTableReader {

    @Test
    public void checkTypeConversion() throws Exception {
        // boolean
        assertEquals("boolean", toColumnType(Types.BIT, "BIT", true).getName());

        // string
        assertEquals("string", toColumnType(Types.CHAR, "CHAR", true).getName());
        assertEquals("string", toColumnType(Types.CHAR, "CHAR", false).getName());
        assertEquals("string", toColumnType(Types.CHAR, "VARCHAR", true).getName());
        assertEquals("string", toColumnType(Types.CHAR, "VARCHAR", false).getName());

        // int
        assertEquals("int", toColumnType(Types.TINYINT, "TINYINT", true).getName());
        assertEquals("int", toColumnType(Types.TINYINT, "TINYINT", false).getName());
        assertEquals("int", toColumnType(Types.SMALLINT, "SMALLINT", true).getName());
        assertEquals("int", toColumnType(Types.SMALLINT, "SMALLINT", false).getName());
        assertEquals("int", toColumnType(Types.INTEGER, "MEDIUMINT", true).getName());
        assertEquals("int", toColumnType(Types.INTEGER, "MEDIUMINT", false).getName());
        assertEquals("int", toColumnType(Types.INTEGER, "INT", true).getName());

        // long
        assertEquals("long", toColumnType(Types.INTEGER, "INT", false).getName());
        assertEquals("long", toColumnType(Types.BIGINT, "BIGINT", true).getName());
        try {
            toColumnType(Types.BIGINT, "BIGINT", false);
            fail();
        } catch (Exception e) {
        }

        // double
        assertEquals("double", toColumnType(Types.FLOAT, "FLOAT", true).getName());
        assertEquals("double", toColumnType(Types.DOUBLE, "DOUBLET", true).getName());
    }
}
