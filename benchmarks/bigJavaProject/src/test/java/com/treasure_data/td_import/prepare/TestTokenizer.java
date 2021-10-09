package com.treasure_data.td_import.prepare;

import org.junit.Test;
import com.treasure_data.td_import.prepare.CSVPrepareConfiguration.NewLine;
import com.treasure_data.td_import.prepare.CSVPrepareConfiguration.Quote;
import com.treasure_data.td_import.reader.CSVRecordReader.Tokenizer;
import org.supercsv.prefs.CsvPreference;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestTokenizer {

    private CSVPrepareConfiguration createConfig(Quote quote, char delimiter, char escape) {
        CSVPrepareConfiguration conf = new CSVPrepareConfiguration();
        conf.setQuoteChar(Quote.DOUBLE);
        conf.setDelimiterChar(delimiter);
        conf.setEscapeChar(escape);
        conf.setNewline(NewLine.LF);
        return conf;
    }

    private CsvPreference createCsvPreference(CSVPrepareConfiguration conf) {
        CsvPreference.Builder b = new CsvPreference.Builder(
                conf.getQuoteChar().quote(),
                conf.getDelimiterChar(),
                conf.getNewline().newline());
        return b.build();
    }

    private Reader createOneLineReader(String line) {
        return new InputStreamReader(new ByteArrayInputStream(line.getBytes()));
    }

    @Test // normal quoted columns
    public void parseWithQuoteMode() throws Exception {
        CSVPrepareConfiguration conf = createConfig(Quote.DOUBLE, ',', '\u0000');
        CsvPreference pref = createCsvPreference(conf);

        Reader reader = createOneLineReader("\"foo\",\"bar\",\"baz\"");
        Tokenizer t = new Tokenizer(conf, reader, pref);

        List<String> columns = new ArrayList<String>();
        t.readColumns(columns);

        assertEquals(Arrays.asList("foo", "bar", "baz"), columns);
    }

    @Test // include escaped quote char
    public void parseColumnIncludesEscapedQuote() throws Exception {
        { // \"\"
            CSVPrepareConfiguration conf = createConfig(Quote.DOUBLE, ',', '\u0000');
            CsvPreference pref = createCsvPreference(conf);

            Reader reader = createOneLineReader("\"fo\"\"o\",\"\"\"bar\",\"baz\"\"\"");
            Tokenizer t = new Tokenizer(conf, reader, pref);

            List<String> columns = new ArrayList<String>();
            t.readColumns(columns);

            assertEquals(Arrays.asList("fo\"o", "\"bar", "baz\""), columns);
        }
        { // \\\": first \" is escaped by custom escape char \\
            CSVPrepareConfiguration conf = createConfig(Quote.DOUBLE, ',', '\\');
            CsvPreference pref = createCsvPreference(conf);

            Reader reader = createOneLineReader("\"fo\\\"o\",\"\\\"bar\",\"baz\\\"\"");
            Tokenizer t = new Tokenizer(conf, reader, pref);

            List<String> columns = new ArrayList<String>();
            t.readColumns(columns);

            assertEquals(Arrays.asList("fo\"o", "\"bar", "baz\""), columns);
        }
    }
}
