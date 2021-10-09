package com.treasure_data.td_import.integration;

import java.io.IOException;

import org.junit.Ignore;

@Ignore
public class TSVFileGenerator extends CSVFileGenerator {

    protected static final String TAB = "\t";

    public TSVFileGenerator(String fileName, String[] header)
            throws IOException {
        super(fileName, header);
    }

    @Override
    public String getDelimiter() {
        return TAB;
    }

}
