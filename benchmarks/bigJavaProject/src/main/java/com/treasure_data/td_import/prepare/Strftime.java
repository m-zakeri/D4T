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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class Strftime {
    private static Properties translate;
    private static final ThreadLocal<SimpleDateFormat> formats = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return null; //new SimpleDateFormat("yyyy/MM/dd"); // dummy object
        }
    };

    static {
        translate = new Properties();
        translate.put("a", "EEE");
        translate.put("A", "EEEE");
        translate.put("b", "MMM");
        translate.put("B", "MMMM");
        translate.put("c", "EEE MMM d HH:mm:ss yyyy");

        //There's no way to specify the century in SimpleDateFormat.  We don't want to hard-code
        //20 since this could be wrong for the pre-2000 files.
        //translate.put("C", "20");
        translate.put("d", "dd");
        translate.put("D", "MM/dd/yy");
        translate.put("e", "dd"); //will show as '03' instead of ' 3'
        translate.put("F", "yyyy-MM-dd");
        translate.put("g", "yy");
        translate.put("G", "yyyy");
        translate.put("H", "HH");
        translate.put("h", "MMM");
        translate.put("I", "hh");
        translate.put("j", "DDD");
        translate.put("k", "HH"); // will show as '07' instead of ' 7'
        translate.put("l", "hh"); //will show as '07' instead of ' 7'
        translate.put("L", "S");
        translate.put("m", "MM");
        translate.put("M", "mm");
        translate.put("n", "\n");
        translate.put("p", "a");
        translate.put("P","a");  //will show as pm instead of PM
        translate.put("r", "hh:mm:ss a");
        translate.put("R","HH:mm");
        //There's no way to specify this with SimpleDateFormat
        //translate.put("s","seconds since ecpoch");
        translate.put("S", "ss");
        translate.put("t", "\t");
        translate.put("T", "HH:mm:ss");
        //There's no way to specify this with SimpleDateFormat
        //translate.put("u","day of week ( 1-7 )");

        //There's no way to specify this with SimpleDateFormat
        //translate.put("U","week in year with first sunday as first day...");

        translate.put("V", "ww"); //I'm not sure this is always exactly the same

        //There's no way to specify this with SimpleDateFormat
        //translate.put("W","week in year with first monday as first day...");

        //There's no way to specify this with SimpleDateFormat
        //translate.put("w","E");
        translate.put("X", "HH:mm:ss");
        translate.put("x", "MM/dd/yy");
        translate.put("y", "yy");
        translate.put("Y", "yyyy");
        translate.put("Z", "z");
        translate.put("z", "Z");
        translate.put("%", "%");
    }

    public Strftime(String format) {
        String convertedFormat = convertDateFormat(format);
        formats.set(new SimpleDateFormat(convertedFormat));
    }

    public long getTime(String t) {
        if (t == null || t.isEmpty()) {
            return 0;
        }

        try {
            return formats.get().parse(t).getTime() / 1000;
        } catch (ParseException e) {
            return 0;
        }
    }

    protected String convertDateFormat(String pattern) {
        boolean inside = false;
        boolean mark = false;
        boolean modifiedCommand = false;

        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);

            if (c == '%' && !mark) {
                mark = true;
            } else {
                if ( mark ) {
                    if (modifiedCommand) {
                        // don't do anything--we just wanted to skip a char
                        modifiedCommand = false;
                        mark = false;
                    } else {
                        inside = translateCommand(buf, pattern, i, inside);
                        // It's a modifier code
                        if (c == 'O' || c == 'E') {
                            modifiedCommand = true;
                        } else {
                            mark = false;
                        }
                    }
                } else {
                    if (!inside && c != ' ') {
                        // We start a literal, which we need to quote
                        buf.append("'");
                        inside = true;
                    }

                    buf.append(c);
                }
            }
        }

        if (buf.length() > 0) {
            char lastChar = buf.charAt(buf.length() - 1);

            if (lastChar != '\'' && inside) {
                buf.append('\'');
            }
        }
        return buf.toString();
    }

    private String quote(String str, boolean insideQuotes) {
        String retVal = str;
        if (!insideQuotes) {
            retVal = '\'' + retVal + '\'';
        }
        return retVal;
    }

    private boolean translateCommand(StringBuffer buf, String pattern,
            int index, boolean oldInside) {
        char firstChar = pattern.charAt(index);
        boolean newInside = oldInside;

        // O and E are modifiers, they mean to present an alternative
        // representation of the next char we just handle the next char
        // as if the O or E wasn't there
        if (firstChar == 'O' || firstChar == 'E') {
            if (index + 1 < pattern.length()) {
                newInside = translateCommand(buf, pattern, index + 1, oldInside);
            } else {
                buf.append(quote("%" + firstChar, oldInside));
            }
        } else {
            String command = translate.getProperty(String.valueOf(firstChar));

            // If we don't find a format, treat it as a literal--That's what apache does
            if (command == null) {
                buf.append(quote("%" + firstChar, oldInside));
            } else {
                // If we were inside quotes, close the quotes
                if (oldInside) {
                    buf.append('\'');
                }
                buf.append(command);
                newInside = false;
            }
        }
        return newInside;
    }
}