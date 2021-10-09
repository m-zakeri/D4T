package com.treasure_data.td_import.integration;

import java.io.IOException;

import org.junit.Ignore;

@Ignore
public class HeaderlessCSVFileGenerator extends CSVFileGenerator {
    public HeaderlessCSVFileGenerator(String fileName, String[] header)
            throws IOException {
        super(fileName, header);
    }

    @Override
    public void writeHeader() throws IOException {
        // header-less
    }

}
