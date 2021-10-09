package com.treasure_data.td_import;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;

public class TestTableImportCommand {

    @Test @Ignore
    public void testPrepareParts01() throws Exception {
        Properties props = System.getProperties();
        props.load(this.getClass().getClassLoader()
                .getResourceAsStream("treasure-data.properties"));

        List<String> opts = new ArrayList<String>();
        //props.setProperty(Config.BI_PREPARE_PARTS_ENCODING, "Shift_JIS");
        //props.setProperty(Configuration.BI_PREPARE_PARTS_TIMEVALUE, "1370941200");
        opts.add("--time-column");
        opts.add("date_code");
        opts.add("--prepare-parallel");
        opts.add("2");
        opts.add("--column-header");
        List<String> args = new ArrayList<String>();
        args.add("prepare_parts");
        args.add("in/from_SQLServer_to_csv_10000000_v01.csv");
//        args.add("./in/from_SQLServer_to_csv_10_v01.csv");
//        args.add("./in/from_SQLServer_to_csv_10_v02.csv");
//        args.add("./in/from_SQLServer_to_csv_10_v03.csv");
//        args.add("./in/from_SQLServer_to_csv_10_v04.csv");
//        args.add("./in/from_SQLServer_to_csv_10_v05.csv");
//        args.add("./in/from_SQLServer_to_csv_10_v06.csv");
//        args.add("./in/from_SQLServer_to_csv_10_v07.csv");
//        args.add("./in/from_SQLServer_to_csv_10_v08.csv");
//        args.add("./in/from_SQLServer_to_csv_10_v09.csv");
//        args.add("./in/TE_JNL_ITM_shiftJIS.csv");

        args.addAll(opts);

        new TableImport(props).tableImport(args.toArray(new String[0]));
    }
}
