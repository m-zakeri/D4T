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
package com.treasure_data.td_import.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class Table {

    public static enum Type {
        LOG("log"), ITEM("item");

        private String type;

        Type(String type) {
            this.type = type;
        }

        public String type() {
            return type;
        }

        public static Type fromString(String type) {
            return StringToType.get(type);
        }

        private static class StringToType {
            private static final Map<String, Type> REVERSE_DICTIONARY;

            static {
                Map<String, Type> map = new HashMap<String, Type>();
                for (Type elem : Type.values()) {
                    map.put(elem.type(), elem);
                }
                REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
            }

            static Type get(String key) {
                return REVERSE_DICTIONARY.get(key);
            }
        }
    }

    protected String databaseName;
    protected String tableName;
    protected Type type;
    protected TableSchema schema;

    public Table(Type type) {
        this.type = type;
    }
}
