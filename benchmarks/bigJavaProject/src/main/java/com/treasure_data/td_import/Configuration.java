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
package com.treasure_data.td_import;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class Configuration implements Constants {

    public static enum Command {
        TABLEIMPORT(CMD_TABLEIMPORT),
        PREPARE(CMD_PREPARE),
        UPLOAD(CMD_UPLOAD),
        AUTO(CMD_AUTO);

        private String name;

        Command(String name) {
            this.name = name;
        }

        public String command() {
            return name;
        }

        public String showHelp(Configuration conf, Properties props) {
            return conf.showHelp(props);
        }

        public static Command fromString(String name) {
            return StringToCommand.get(name);
        }

        private static class StringToCommand {
            private static final Map<String, Command> REVERSE_DICTIONARY;

            static {
                Map<String, Command> map = new HashMap<String, Command>();
                for (Command elem : Command.values()) {
                    map.put(elem.command(), elem);
                }
                REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
            }

            static Command get(String key) {
                return REVERSE_DICTIONARY.get(key);
            }
        }
    }

    public String showHelp(Properties props) {
        throw new UnsupportedOperationException();
    }

    public static String getVersion() {
        return getTDImportVersion();
    }

    public static String getTDImportVersion() {
        return getVersion("TD-Import-Version");
    }

    public static String getTDClientVersion() {
        return getVersion("TD-Client-Version");
    }

    static String getVersion(String tag) {
        String version = "";

        Class<BulkImportCommand> c = BulkImportCommand.class;
        String className = c.getSimpleName() + ".class";
        String classPath = c.getResource(className).toString();

        if (!classPath.startsWith("jar")) {
            return version;
        }

        String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) +
                "/META-INF/MANIFEST.MF";
        try {
            Manifest manifest = new Manifest(new URL(manifestPath).openStream());
            Attributes attr = manifest.getMainAttributes();
            if ((version = attr.getValue(tag)) != null) {
                return version;
            } else {
                return "";
            }
        } catch (IOException e) {
            return version;
        }
    }
}
