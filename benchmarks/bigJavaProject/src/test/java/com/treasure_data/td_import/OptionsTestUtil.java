package com.treasure_data.td_import;

import java.util.Properties;

import org.junit.Ignore;

import com.treasure_data.td_import.Options;

@Ignore
public class OptionsTestUtil {

    public static Options createPrepareOptions(Properties props) {
        Options options = new Options();
        options.initPrepareOptionParser(props);
        return options;
    }

    public static Options createPrepareOptions(Properties props, String[] args) {
        Options options = createPrepareOptions(props);
        options.setOptions(args);
        return options;
    }

    public static Options createUploadOptions(Properties props) {
        Options options = new Options();
        options.initUploadOptionParser(props);
        return options;
    }

    public static Options createUploadOptions(Properties props, String[] args) {
        Options options = createUploadOptions(props);
        options.setOptions(args);
        return options;
    }
}
