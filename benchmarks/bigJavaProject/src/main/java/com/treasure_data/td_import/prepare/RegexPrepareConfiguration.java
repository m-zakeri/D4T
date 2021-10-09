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

public class RegexPrepareConfiguration extends FixedColumnsPrepareConfiguration {
    private static final Logger LOG = Logger
            .getLogger(RegexPrepareConfiguration.class.getName());

    protected String regexPattern;

    public RegexPrepareConfiguration() {
    }

    @Override
    public void configure(Properties props, Options options) {
        super.configure(props, options);

        setColumnNames();
        setColumnTypes();
        setRegexPattern();
    }

    public void setRegexPattern() {
        if (!optionSet.has(BI_PREPARE_PARTS_REGEX_PATTERN)) {
            throw new IllegalArgumentException(String.format(
                    "Not specify regex pattern. '%s' option is required", BI_PREPARE_PARTS_REGEX_PATTERN));
        } else {
            regexPattern = (String) optionSet.valueOf(BI_PREPARE_PARTS_REGEX_PATTERN);
        }
    }
    public String getRegexPattern() {
        return regexPattern;
    }

    @Override
    public void setColumnHeader() {
        hasColumnHeader = false;
    }
}
