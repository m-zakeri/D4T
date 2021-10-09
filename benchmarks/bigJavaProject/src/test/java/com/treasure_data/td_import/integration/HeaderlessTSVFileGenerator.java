package com.treasure_data.td_import.integration;

import java.io.IOException;

import org.junit.Ignore;

@Ignore
public class HeaderlessTSVFileGenerator extends TSVFileGenerator {

    public HeaderlessTSVFileGenerator(String fileName, String[] header)
            throws IOException {
        super(fileName, header);
    }

    @Override
    public void writeHeader() throws IOException {
        // header-less
    }
}
