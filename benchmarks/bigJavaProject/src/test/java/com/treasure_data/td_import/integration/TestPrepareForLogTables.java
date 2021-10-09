package com.treasure_data.td_import.integration;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.treasure_data.td_import.prepare.PrepareConfiguration.Format;

public class TestPrepareForLogTables extends PrepareIntegrationTestUtil {
    @Before
    public void createResources() throws Exception {
        super.createResources();
    }

    @After
    public void destroyResources() throws Exception {
        super.destroyResources();
    }

    @Test
    public void writeFromCSVWithTimeColumn() throws Exception {
        setOptions(Format.CSV.format(), true, null, null, null, null, null);
        preparePartsFromCSVWithTimeColumn();

        refleshOptions();
        setOptions(Format.CSV.format(), true, null, null, null, "timestamp", null);
        preparePartsFromCSVWithTimeColumn();

        refleshOptions();
        setOptions(Format.CSV.format(), true, null, null, null, null, "string_value,int_value,double_value,time");
        preparePartsFromCSVWithTimeColumn();
    }

    @Test
    public void writeFromCSVWithAlasTimeColumn() throws Exception {
        setOptions(Format.CSV.format(), true, "timestamp", null, null, null, null);
        preparePartsFromCSVWithAlasTimeColumn();

        refleshOptions();
        setOptions(Format.CSV.format(), true, "timestamp", null, null, "timestamp", null);
        preparePartsFromCSVWithAlasTimeColumn();

        refleshOptions();
        setOptions(Format.CSV.format(), true, "timestamp", null, null, null, "string_value,int_value,double_value,time");
        preparePartsFromCSVWithAlasTimeColumn();
    }

    @Test
    public void writeFromCSVWithTimeFormat() throws Exception {
        setOptions(Format.CSV.format(), true, "timeformat", "%Y-%m-%d %H:%M:%S %z", null, null, null);
        preparePartsFromCSVWithTimeFormat();

        refleshOptions();
        setOptions(Format.CSV.format(), true, "timeformat", "%Y-%m-%d %H:%M:%S %z", null, "timeformat", null);
        preparePartsFromCSVWithTimeFormat();

        refleshOptions();
        setOptions(Format.CSV.format(), true, "suggested_timeformat", null, null, null, null);
        preparePartsFromCSVWithSuggestedTimeFormat();

        refleshOptions();
        setOptions(Format.CSV.format(), true, "timeformat", "%Y-%m-%d %H:%M:%S %z", null, null, "string_value,int_value,double_value,time");
        preparePartsFromCSVWithTimeFormat();
    }

    @Test
    public void writeFromHeaderlessCSVWithTimeColumn() throws Exception {
        setOptions(Format.CSV.format(), false, null, null, "string_value,int_value,double_value,timestamp,time", null, null);
        preparePartsFromHeaderlessCSVWithTimeColumn();

        refleshOptions();
        setOptions(Format.CSV.format(), false, null, null, "string_value,int_value,double_value,timestamp,time", "timestamp", null);
        preparePartsFromHeaderlessCSVWithTimeColumn();

        refleshOptions();
        setOptions(Format.CSV.format(), false, null, null, "string_value,int_value,double_value,timestamp,time", null, "string_value,int_value,double_value,time");
        preparePartsFromHeaderlessCSVWithTimeColumn();
    }

    @Test
    public void writeFromHeaderlessCSVWithAlasTimeColumn() throws Exception {
        setOptions(Format.CSV.format(), false, "timestamp", null, "string_value,int_value,double_value,timestamp", null, null);
        preparePartsFromHeaderlessCSVWithAlasTimeColumn();

        refleshOptions();
        setOptions(Format.CSV.format(), false, "timestamp", null, "string_value,int_value,double_value,timestamp", "timestamp", null);
        preparePartsFromHeaderlessCSVWithAlasTimeColumn();

        refleshOptions();
        setOptions(Format.CSV.format(), false, "timestamp", null, "string_value,int_value,double_value,timestamp", null, "string_value,int_value,double_value,time");
        preparePartsFromHeaderlessCSVWithAlasTimeColumn();
    }

    @Test
    public void writeFromHeaderlessCSVWithTimeFormat() throws Exception {
        setOptions(Format.CSV.format(), false, "timeformat", "%Y-%m-%d %H:%M:%S %z", "string_value,int_value,double_value,timeformat", null, null);
        preparePartsFromHeaderlessCSVWithTimeFormat();

        refleshOptions();
        setOptions(Format.CSV.format(), false, "timeformat", "%Y-%m-%d %H:%M:%S %z", "string_value,int_value,double_value,timeformat", "timeformat", null);
        preparePartsFromHeaderlessCSVWithTimeFormat();

        refleshOptions();
        setOptions(Format.CSV.format(), false, "timeformat", "%Y-%m-%d %H:%M:%S %z", "string_value,int_value,double_value,timeformat", null, "string_value,int_value,double_value,time");
        preparePartsFromHeaderlessCSVWithTimeFormat();

        refleshOptions();
        setOptions(Format.CSV.format(), false, "suggested_timeformat", null, "string_value,int_value,double_value,suggested_timeformat", null, null);
        preparePartsFromHeaderlessCSVWithSuggestedTimeFormat();
    }

    @Test
    public void writeFromTSVWithTimeColumn() throws Exception {
        setOptions(Format.TSV.format(), true, null, null, null, null, null);
        preparePartsFromTSVWithTimeColumn();

        refleshOptions();
        setOptions(Format.TSV.format(), true, null, null, null, "timestamp", null);
        preparePartsFromTSVWithTimeColumn();

        refleshOptions();
        setOptions(Format.TSV.format(), true, null, null, null, null, "string_value,int_value,double_value,time");
        preparePartsFromTSVWithTimeColumn();
    }

    @Test
    public void writeFromTSVWithAlasTimeColumn() throws Exception {
        setOptions(Format.TSV.format(), true, "timestamp", null, null, null, null);
        preparePartsFromTSVWithAlasTimeColumn();

        refleshOptions();
        setOptions(Format.TSV.format(), true, "timestamp", null, null, "timestamp", null);
        preparePartsFromTSVWithAlasTimeColumn();

        refleshOptions();
        setOptions(Format.TSV.format(), true, "timestamp", null, null, null, "string_value,int_value,double_value,time");
        preparePartsFromTSVWithAlasTimeColumn();
    }

    @Test
    public void writeFromTSVWithTimeFormat() throws Exception {
        setOptions(Format.TSV.format(), true, "timeformat", "%Y-%m-%d %H:%M:%S %z", null, null, null);
        preparePartsFromTSVWithTimeFormat();

        refleshOptions();
        setOptions(Format.TSV.format(), true, "timeformat", "%Y-%m-%d %H:%M:%S %z", null, "timeformat", null);
        preparePartsFromTSVWithTimeFormat();

        refleshOptions();
        setOptions(Format.TSV.format(), true, "timeformat", "%Y-%m-%d %H:%M:%S %z", null, null, "string_value,int_value,double_value,time");
        preparePartsFromTSVWithTimeFormat();
    }

    @Test
    public void writeFromHeaderlessTSVWithTimeColumn() throws Exception {
        setOptions(Format.TSV.format(), false, null, null, "string_value,int_value,double_value,timestamp,time", null, null);
        preparePartsFromHeaderlessTSVWithTimeColumn();

        refleshOptions();
        setOptions(Format.TSV.format(), false, null, null, "string_value,int_value,double_value,timestamp,time", "timestamp", null);
        preparePartsFromHeaderlessTSVWithTimeColumn();

        refleshOptions();
        setOptions(Format.TSV.format(), false, null, null, "string_value,int_value,double_value,timestamp,time", null, "string_value,int_value,double_value,time");
        preparePartsFromHeaderlessTSVWithTimeColumn();
    }

    @Test
    public void writeFromHeaderlessTSVWithAlasTimeColumn() throws Exception {
        setOptions(Format.TSV.format(), false, "timestamp", null, "string_value,int_value,double_value,timestamp", null, null);
        preparePartsFromHeaderlessTSVWithAlasTimeColumn();

        refleshOptions();
        setOptions(Format.TSV.format(), false, "timestamp", null, "string_value,int_value,double_value,timestamp", "timestamp", null);
        preparePartsFromHeaderlessTSVWithAlasTimeColumn();

        refleshOptions();
        setOptions(Format.TSV.format(), false, "timestamp", null, "string_value,int_value,double_value,timestamp", null, "string_value,int_value,double_value,time");
        preparePartsFromHeaderlessTSVWithAlasTimeColumn();
    }

    @Test
    public void writeFromHeaderlessTSVWithTimeFormat() throws Exception {
        setOptions(Format.TSV.format(), false, "timeformat", "%Y-%m-%d %H:%M:%S %z", "string_value,int_value,double_value,timeformat", null, null);
        preparePartsFromHeaderlessTSVWithTimeFormat();

        refleshOptions();
        setOptions(Format.TSV.format(), false, "timeformat", "%Y-%m-%d %H:%M:%S %z", "string_value,int_value,double_value,timeformat", "timeformat", null);
        preparePartsFromHeaderlessTSVWithTimeFormat();

        refleshOptions();
        setOptions(Format.TSV.format(), false, "timeformat", "%Y-%m-%d %H:%M:%S %z", "string_value,int_value,double_value,timeformat", null, "string_value,int_value,double_value,time");
        preparePartsFromHeaderlessTSVWithTimeFormat();
    }

//    @Test // TODO
//    public void writeFromSyslog() throws Exception {
//        setProperties(Format.SYSLOG.format(), false, null, null, null, null, null);
//        preparePartsFromSyslog();
//    }

//    @Test // TODO
//    public void writeFromApacheLog() throws Exception {
//        setOptions(Format.APACHE.format(), false, null, null, null, null, null);
//        preparePartsFromApacheLog();
//    }

    @Test
    public void writeFromJSONWithTimeColumn() throws Exception {
        setOptions(Format.JSON.format(), false, null, null, null, null, null);
        preparePartsFromJSONWithTimeColumn();

        refleshOptions();
        setOptions(Format.JSON.format(), false, null, null, null, "timestamp", null);
        preparePartsFromJSONWithTimeColumn();

        refleshOptions();
        setOptions(Format.JSON.format(), false, null, null, null, null, "string_value,int_value,double_value,time");
        preparePartsFromJSONWithTimeColumn();
    }

    @Test
    public void writeFromJSONWithAlasTimeColumn() throws Exception {
        setOptions(Format.JSON.format(), false, "timestamp", null, null, null, null);
        preparePartsFromJSONWithAlasTimeColumn();

        refleshOptions();
        setOptions(Format.JSON.format(), false, "timestamp", null, null, "timestamp", null);
        preparePartsFromJSONWithAlasTimeColumn();

        refleshOptions();
        setOptions(Format.JSON.format(), false, "timestamp", null, null, null, "string_value,int_value,double_value,time");
        preparePartsFromJSONWithAlasTimeColumn();
    }

    @Test
    public void writeFromJSONWithTimeFormat() throws Exception {
        setOptions(Format.JSON.format(), false, "timeformat", "%Y-%m-%d %H:%M:%S %z", null, null, null);
        preparePartsFromJSONWithTimeFormat();

        refleshOptions();
        setOptions(Format.JSON.format(), false, "timeformat", "%Y-%m-%d %H:%M:%S %z", null, "timeformat", null);
        preparePartsFromJSONWithTimeFormat();

        refleshOptions();
        setOptions(Format.JSON.format(), false, "timeformat", "%Y-%m-%d %H:%M:%S %z", null, null, "string_value,int_value,double_value,time");
        preparePartsFromJSONWithTimeFormat();
    }

    @Test
    public void writeFromMessagePackWithTimeColumn() throws Exception {
        setOptions(Format.MSGPACK.format(), false, null, null, null, null, null);
        preparePartsFromMessagePackWithTimeColumn();

        refleshOptions();
        setOptions(Format.MSGPACK.format(), false, null, null, null, "timestamp", null);
        preparePartsFromMessagePackWithTimeColumn();

        refleshOptions();
        setOptions(Format.MSGPACK.format(), false, null, null, null, null, "string_value,int_value,double_value,time");
        preparePartsFromMessagePackWithTimeColumn();
    }

    @Test
    public void writeFromMessagePackWithAlasTimeColumn() throws Exception {
        setOptions(Format.MSGPACK.format(), false, "timestamp", null, null, null, null);
        preparePartsFromMessagePackWithAlasTimeColumn();

        refleshOptions();
        setOptions(Format.MSGPACK.format(), false, "timestamp", null, null, "timestamp", null);
        preparePartsFromMessagePackWithAlasTimeColumn();

        refleshOptions();
        setOptions(Format.MSGPACK.format(), false, "timestamp", null, null, null, "string_value,int_value,double_value,time");
        preparePartsFromMessagePackWithAlasTimeColumn();
    }

    @Test
    public void writeFromMessagePackWithTimeFormat() throws Exception {
        setOptions(Format.MSGPACK.format(), false, "timeformat", "%Y-%m-%d %H:%M:%S %z", null, null, null);
        preparePartsFromMessagePackWithTimeFormat();

        refleshOptions();
        setOptions(Format.MSGPACK.format(), false, "timeformat", "%Y-%m-%d %H:%M:%S %z", null, "timeformat", null);
        preparePartsFromMessagePackWithTimeFormat();

        refleshOptions();
        setOptions(Format.MSGPACK.format(), false, "timeformat", "%Y-%m-%d %H:%M:%S %z", null, null, "string_value,int_value,double_value,time");
        preparePartsFromMessagePackWithTimeFormat();
    }

}
