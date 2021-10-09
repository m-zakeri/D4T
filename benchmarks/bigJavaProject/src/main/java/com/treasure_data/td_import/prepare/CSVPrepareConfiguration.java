//
// Treasure Data Bulk-Import Tool in Java
//
// Copyright (C) 2012 - 2013 Muga Nishizawa
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
package com.treasure_data.td_import.prepare;

import java.util.Properties;
import java.util.logging.Logger;

import com.treasure_data.td_import.Options;
import com.treasure_data.td_import.Configuration;

public class CSVPrepareConfiguration extends FixedColumnsPrepareConfiguration {

    public static enum Quote {
        DOUBLE("\""), SINGLE("'"), NONE("\u0000");

        private String quote;

        Quote(String quote) {
            this.quote = quote;
        }

        public char quote() {
            return quote.charAt(0);
        }
    }

    public static enum NewLine {
        CR("\r"), LF("\n"), CRLF("\r\n");

        private String newline;

        NewLine(String newline) {
            this.newline = newline;
        }

        public String newline() {
            return newline;
        }
    }

    private static final Logger LOG = Logger.getLogger(CSVPrepareConfiguration.class.getName());

    protected char delimiterChar;
    protected char escapeChar;
    protected Quote quoteChar;
    protected NewLine newline;
    protected String typeErrorMode;

    public CSVPrepareConfiguration() {
        super();
    }

    @Override
    public void configure(Properties props, Options options) {
        super.configure(props, options);

        // delimiter
        setDelimiterChar();

        // escape
        setEscapeChar();

        // quote
        setQuoteChar();

        // newline
        setNewline();

        // column-header
        setColumnHeader();

        // column-names
        setColumnNames();

        // column-types
        setColumnTypes();

        // column-type
        setColumnTypeMap();
    }

    public void setDelimiterChar() {
        String delim;
        if (!optionSet.has("delimiter")) {
            if (format.equals(Format.CSV)) { // 'csv'
                delim = Configuration.BI_PREPARE_PARTS_DELIMITER_CSV_DEFAULTVALUE;
            } else { // 'tsv'
                delim = Configuration.BI_PREPARE_PARTS_DELIMITER_TSV_DEFAULTVALUE;
            }
        } else {
            delim = (String) optionSet.valueOf("delimiter");
        }
        delimiterChar = delim.charAt(0);
    }

    //@VisibleForTesting
    public void setDelimiterChar(char delimiter) {
        delimiterChar = delimiter;
    }

    public char getDelimiterChar() {
        return delimiterChar;
    }

    public void setEscapeChar() {
        String escape;
        if (!optionSet.has("escape")) {
            escape = "\\"; // default value is '\'. not '"'
        } else {
            escape = (String) optionSet.valueOf("escape");
            if (escape.isEmpty()) {
                escape = "\u0000";
            }
        }
        escapeChar = escape.charAt(0);
    }

    //@VisibleForTesting
    public void setEscapeChar(char escape) {
        escapeChar = escape;
    }

    public char getEscapeChar() {
        return escapeChar;
    }

    public void setQuoteChar() {
        String quote;
        if (!optionSet.has("quote")) {
            if (format.equals(Format.CSV)) { // 'csv'
                quote = Configuration.BI_PREPARE_PARTS_QUOTE_CSV_DEFAULTVALUE;
            } else { // 'tsv'
                quote = Configuration.BI_PREPARE_PARTS_QUOTE_TSV_DEFAULTVALUE;
            }
        } else {
            quote = (String) optionSet.valueOf("quote");
        }

        try {
            quoteChar = Quote.valueOf(quote);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("unsupported quote char: " + quote, e);
        }
    }

    //@VisibleForTesting
    public void setQuoteChar(Quote quote) {
        this.quoteChar = quote;
    }

    public Quote getQuoteChar() {
        return quoteChar;
    }

    public void setNewline() {
        String nline;
        if (!optionSet.has("newline")) {
            nline = Configuration.BI_PREPARE_PARTS_NEWLINE_DEFAULTVALUE;
        } else {
            nline = (String) optionSet.valueOf("newline");
        }

        try {
            newline = NewLine.valueOf(nline);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("unsupported newline char: " + nline, e);
        }
    }

    //@VisibleForTesting
    public void setNewline(NewLine newline) {
        this.newline = newline;
    }

    public NewLine getNewline() {
        return newline;
    }

    @Override
    public void setColumnHeader() {
        hasColumnHeader = optionSet.has("column-header");
    }

    @Override
    public void setColumnNames() {
        if (optionSet.has(BI_PREPARE_PARTS_COLUMNS)) {
            String[] cnames = optionSet.valuesOf(BI_PREPARE_PARTS_COLUMNS).toArray(new String[0]);
            setColumnNames(cnames);
        } else if (!hasColumnHeader()) {
            throw new IllegalArgumentException("Column names not set");
        }
    }
}
