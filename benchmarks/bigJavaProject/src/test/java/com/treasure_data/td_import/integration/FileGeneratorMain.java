package com.treasure_data.td_import.integration;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;

@Ignore
public class FileGeneratorMain {
    public static void main(String[] args) throws Exception {
        List<FileGenerator> gens = new ArrayList<FileGenerator>();
        String dirName = "./src/test/resources/in/";

        // CSV files
        gens.add(new CSVFileGenerator(dirName + "csvfile-with-time.csv",
                new String[] { "string_value", "int_value", "double_value", "timestamp", "time" }));
        gens.add(new CSVFileGenerator(dirName + "csvfile-with-aliastime.csv",
                new String[] { "string_value", "int_value", "double_value", "timestamp" }));
        gens.add(new CSVFileGenerator(dirName + "csvfile-with-timeformat.csv",
                new String[] { "string_value", "int_value", "double_value", "timeformat" }));
        gens.add(new CSVFileGenerator(dirName + "csvfile-with-suggested-timeformat.csv",
                new String[] { "string_value", "int_value", "double_value", "suggested_timeformat" }));

        // header-less CSV files
        gens.add(new HeaderlessCSVFileGenerator(dirName + "headerless-csvfile-with-time.csv",
                new String[] { "string_value", "int_value", "double_value", "timestamp", "time" }));
        gens.add(new HeaderlessCSVFileGenerator(dirName + "headerless-csvfile-with-aliastime.csv",
                new String[] { "string_value", "int_value", "double_value", "timestamp" }));
        gens.add(new HeaderlessCSVFileGenerator(dirName + "headerless-csvfile-with-timeformat.csv",
                new String[] { "string_value", "int_value", "double_value", "timeformat" }));
        gens.add(new HeaderlessCSVFileGenerator(dirName + "headerless-csvfile-with-suggested-timeformat.csv",
                new String[] { "string_value", "int_value", "double_value", "suggested_timeformat" }));

        // TSV files
        gens.add(new TSVFileGenerator(dirName + "tsvfile-with-time.tsv",
                new String[] { "string_value", "int_value", "double_value", "timestamp", "time" }));
        gens.add(new TSVFileGenerator(dirName + "tsvfile-with-aliastime.tsv",
                new String[] { "string_value", "int_value", "double_value", "timestamp" }));
        gens.add(new TSVFileGenerator(dirName + "tsvfile-with-timeformat.tsv",
                new String[] { "string_value", "int_value", "double_value", "timeformat" }));

        // header-less TSV files
        gens.add(new HeaderlessTSVFileGenerator(dirName + "headerless-tsvfile-with-time.tsv",
                new String[] { "string_value", "int_value", "double_value", "timestamp", "time" }));
        gens.add(new HeaderlessTSVFileGenerator(dirName + "headerless-tsvfile-with-aliastime.tsv",
                new String[] { "string_value", "int_value", "double_value", "timestamp" }));
        gens.add(new HeaderlessTSVFileGenerator(dirName + "headerless-tsvfile-with-timeformat.tsv",
                new String[] { "string_value", "int_value", "double_value", "timeformat" }));

        // syslog files
        gens.add(new SyslogFileGenerator(dirName + "syslogfile.syslog",
                new String[] { "string_value", "int_value", "time" }));

        // apache files
        gens.add(new ApacheFileGenerator(dirName + "apachelogfile.apache",
                new String[] { "string_value", "int_value", "time" }));

        // JSON files
        gens.add(new JSONFileGenerator(dirName + "jsonfile-with-time.json",
                new String[] { "string_value", "int_value", "double_value", "timestamp", "time" }));
        gens.add(new JSONFileGenerator(dirName + "jsonfile-with-aliastime.json",
                new String[] { "string_value", "int_value", "double_value", "timestamp" }));
        gens.add(new JSONFileGenerator(dirName + "jsonfile-with-timeformat.json",
                new String[] { "string_value", "int_value", "double_value", "timeformat" }));

        // MessagePack files
        gens.add(new MessagePackFileGenerator(dirName + "msgpackfile-with-time.msgpack",
                new String[] { "string_value", "int_value", "double_value", "timestamp", "time" }));
        gens.add(new MessagePackFileGenerator(dirName + "msgpackfile-with-aliastime.msgpack",
                new String[] { "string_value", "int_value", "double_value", "timestamp" }));
        gens.add(new MessagePackFileGenerator(dirName + "msgpackfile-with-timeformat.msgpack",
                new String[] { "string_value", "int_value", "double_value", "timeformat" }));

        // training data file
        gens.add(new TrainingDataFileGenerator(dirName + "trainingfile-with-time.msgpack.gz",
                new String[] { "string_value", "int_value", "double_value", "timestamp", "time" }));

        long numRows = 5000;
        long baseTime = 1372305600;

        String[]  availableHeader = new String[] {
                "string_value", "int_value", "double_value", "time", "timestamp", "timeformat", "suggested_timeformat",
        };
        TrainingDataSet dataset = new TrainingDataSet(numRows, baseTime, availableHeader);
        dataset.createDataFiles(gens.toArray(new FileGenerator[0]));
    }
}
