package com.treasure_data.td_import;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;

public class TestBulkImportCommand {

    @Test @Ignore
    public void testPrepareParts01() throws Exception {
        Properties props = System.getProperties();

        List<String> opts = new ArrayList<String>();
        opts.add("--time-column");
        opts.add("date_code");
        opts.add("--column-header");
        List<String> args = new ArrayList<String>();
        args.add("prepare");
        args.add("./in/from_SQLServer_to_csv_10_v01.csv");
        args.addAll(opts);

        new BulkImportCommand(props).doPrepareCommand(args.toArray(new String[0]));
    }

    @Test @Ignore
    public void testUploadParts01() throws Exception {
        Properties props = System.getProperties();
        props.load(this.getClass().getClassLoader()
                .getResourceAsStream("treasure-data.properties"));

        List<String> opts = new ArrayList<String>();
        //props.setProperty(Config.BI_PREPARE_PARTS_TIMECOLUMN, "date_code");
        //props.setProperty(Config.BI_PREPARE_PARTS_ENCODING, "Shift_JIS");
        opts.add("--parallel");
        opts.add("3");

        List<String> args = new ArrayList<String>();
        args.add("upload_parts");
        args.add("mugasess");
        //args.add("out/from_SQLServer_to_csv_10000000_v01_csv_0.msgpack.gz");
        args.add("out/from_SQLServer_to_csv_10_v01_csv_0.msgpack.gz");
        args.add("out/from_SQLServer_to_csv_10_v02_csv_0.msgpack.gz");

        args.addAll(opts);

        new BulkImportCommand(props).doUploadCommand(args.toArray(new String[0]));
    }

    @Test @Ignore
    public void testPrepareUploadParts01() throws Exception {
        Properties props = System.getProperties();
        props.load(this.getClass().getClassLoader()
                .getResourceAsStream("treasure-data.properties"));

        List<String> opts = new ArrayList<String>();
        opts.add("--column-header");
        opts.add("--time-column");
        opts.add("date_code");
        //props.setProperty(Config.BI_PREPARE_PARTS_ENCODING, "Shift_JIS");
        //props.setProperty(Configuration.BI_PREPARE_PARTS_TIMEVALUE, "1370941200");
        opts.add("--prepare-parallel");
        opts.add("8");

        List<String> args = new ArrayList<String>();
        args.add("upload_parts");
        args.add("mugasess");
        args.add("./in/from_SQLServer_to_csv_10_v01.csv");
        args.add("./in/from_SQLServer_to_csv_10_v02.csv");

        args.addAll(opts);

        new BulkImportCommand(props).doUploadCommand(args.toArray(new String[0]));
    }

//    @Test @Ignore
//    public void testPrepareUploadParts02() throws Exception {
//        Properties props = System.getProperties();
//        props.load(this.getClass().getClassLoader()
//                .getResourceAsStream("treasure-data.properties"));
//
//        //props.setProperty(Configuration.BI_PREPARE_PARTS_TIMECOLUMN, "date_code");
//        //props.setProperty(Config.BI_PREPARE_PARTS_ENCODING, "Shift_JIS");
//        //props.setProperty(Configuration.BI_PREPARE_PARTS_TIMEVALUE, "1370941200");
//        //props.setProperty(Configuration.BI_PREPARE_PARTS_PARALLEL, "2");
//        props.setProperty(Configuration.BI_PREPARE_PARTS_FORMAT, "mysql");
//        props.setProperty(Configuration.BI_PREPARE_PARTS_JDBC_CONNECTION_URL, props.getProperty("mysql.test.url"));
//        props.setProperty(Configuration.BI_PREPARE_PARTS_JDBC_USER, props.getProperty("mysql.test.user"));
//        props.setProperty(Configuration.BI_PREPARE_PARTS_JDBC_PASSWORD, props.getProperty("mysql.test.password"));
//        props.setProperty(Configuration.BI_PREPARE_PARTS_JDBC_TABLE, props.getProperty("mysql.test.table"));
//        final String[] args = new String[] {
//                "upload_parts",
//                "mugasess",
//                "mugatbl"
//        };
//
//        BulkImportMain.prepareAndUploadParts(args, props);
//    }
}
