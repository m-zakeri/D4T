package com.treasure_data.td_import.integration;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.treasure_data.td_import.prepare.PrepareConfiguration.Format;

public class TestPrepareForItemTables extends PrepareIntegrationTestUtil {
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
        setItemTableOptions(Format.CSV.format(), true, "time:int", null, null, null);
        preparePartsFromCSVWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.CSV.format(), true, "string_value:string", null, null, null);
        preparePartsFromCSVWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.CSV.format(), true, "time:int", null, "timestamp", null);
        preparePartsFromCSVWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.CSV.format(), true, "string_value:string", null, "timestamp", null);
        preparePartsFromCSVWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.CSV.format(), true, "time:int", null, null, "string_value,int_value,double_value,time");
        preparePartsFromCSVWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.CSV.format(), true, "string_value:string", null, null, "string_value,int_value,double_value,time");
        preparePartsFromCSVWithTimeColumn();
    }

    @Test
    public void writeFromHeaderlessCSVWithTimeColumn() throws Exception {
        setItemTableOptions(Format.CSV.format(), false, "time:int", "string_value,int_value,double_value,timestamp,time", null, null);
        preparePartsFromHeaderlessCSVWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.CSV.format(), false, "string_value:string", "string_value,int_value,double_value,timestamp,time", null, null);
        preparePartsFromHeaderlessCSVWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.CSV.format(), false, "time:int", "string_value,int_value,double_value,timestamp,time", "timestamp", null);
        preparePartsFromHeaderlessCSVWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.CSV.format(), false, "string_value:string", "string_value,int_value,double_value,timestamp,time", "timestamp", null);
        preparePartsFromHeaderlessCSVWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.CSV.format(), false, "time:int", "string_value,int_value,double_value,timestamp,time", null, "string_value,int_value,double_value,time");
        preparePartsFromHeaderlessCSVWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.CSV.format(), false, "string_value:string", "string_value,int_value,double_value,timestamp,time", null, "string_value,int_value,double_value,time");
        preparePartsFromHeaderlessCSVWithTimeColumn();
    }

    @Test
    public void writeFromTSVWithTimeColumn() throws Exception {
        setItemTableOptions(Format.TSV.format(), true, "time:int", null, null, null);
        preparePartsFromTSVWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.TSV.format(), true, "string_value:string", null, null, null);
        preparePartsFromTSVWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.TSV.format(), true, "time:int", null, "timestamp", null);
        preparePartsFromTSVWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.TSV.format(), true, "string_value:string", null, "timestamp", null);
        preparePartsFromTSVWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.TSV.format(), true, "time:int", null, null, "string_value,int_value,double_value,time");
        preparePartsFromTSVWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.TSV.format(), true, "string_value:string", null, null, "string_value,int_value,double_value,time");
        preparePartsFromTSVWithTimeColumn();
    }

    @Test
    public void writeFromHeaderlessTSVWithTimeColumn() throws Exception {
        setItemTableOptions(Format.TSV.format(), false, "time:int", "string_value,int_value,double_value,timestamp,time", null, null);
        preparePartsFromHeaderlessTSVWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.TSV.format(), false, "string_value:string", "string_value,int_value,double_value,timestamp,time", null, null);
        preparePartsFromHeaderlessTSVWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.TSV.format(), false, "time:int", "string_value,int_value,double_value,timestamp,time", "timestamp", null);
        preparePartsFromHeaderlessTSVWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.TSV.format(), false, "string_value:string", "string_value,int_value,double_value,timestamp,time", "timestamp", null);
        preparePartsFromHeaderlessTSVWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.TSV.format(), false, "time:int", "string_value,int_value,double_value,timestamp,time", null, "string_value,int_value,double_value,time");
        preparePartsFromHeaderlessTSVWithTimeColumn();
    }

////    @Test // TODO
////    public void writeFromSyslog() throws Exception {
////        setProperties(Format.SYSLOG.format(), false, null, null, null, null, null);
////        preparePartsFromSyslog();
////    }
//
////    @Test
////    public void writeFromApacheLog() throws Exception {
////        setOptions(Format.APACHE.format(), false, null, null, null, null, null);
////        preparePartsFromApacheLog();
////    }

    @Test
    public void writeFromJSONWithTimeColumn() throws Exception {
        setItemTableOptions(Format.JSON.format(), false, "time:int", null, null, null);
        preparePartsFromJSONWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.JSON.format(), false, "string_value:string", null, null, null);
        preparePartsFromJSONWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.JSON.format(), false, "time:int", null, "timestamp", null);
        preparePartsFromJSONWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.JSON.format(), false, "string_value:string", null, "timestamp", null);
        preparePartsFromJSONWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.JSON.format(), false, "time:int", null, null, "string_value,int_value,double_value,time");
        preparePartsFromJSONWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.JSON.format(), false, "string_value:string", null, null, "string_value,int_value,double_value,time");
        preparePartsFromJSONWithTimeColumn();
    }

    @Test
    public void writeFromMessagePackWithTimeColumn() throws Exception {
        setItemTableOptions(Format.MSGPACK.format(), false, "time:int", null, null, null);
        preparePartsFromMessagePackWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.MSGPACK.format(), false, "string_value:string", null, null, null);
        preparePartsFromMessagePackWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.MSGPACK.format(), false, "time:int", null, "timestamp", null);
        preparePartsFromMessagePackWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.MSGPACK.format(), false, "string_value:string", null, "timestamp", null);
        preparePartsFromMessagePackWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.MSGPACK.format(), false, "time:int", null, null, "string_value,int_value,double_value,time");
        preparePartsFromMessagePackWithTimeColumn();

        refleshOptions();
        setItemTableOptions(Format.MSGPACK.format(), false, "string_value:string", null, null, "string_value,int_value,double_value,time");
        preparePartsFromMessagePackWithTimeColumn();
    }

}
