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
package com.treasure_data.td_import.source;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Source {

    public static enum Type {
        FILE("file") {
            @Override
            public List<Source> createSources(SourceDesc desc) {
                return LocalFileSource.createSources(desc);
            }
        },
        S3("s3") {
            @Override
            public List<Source> createSources(SourceDesc desc) {
                return S3Source.createSources(desc);
            }
        },
        MYSQL("mysql") {
            @Override
            public List<Source> createSources(SourceDesc desc) {
                return MysqlSource.createSources(desc);
            }
        };

        private String type;

        Type(String type) {
            this.type = type;
        }

        public String type() {
            return type;
        }

        public abstract List<Source> createSources(SourceDesc desc);

        public static Type fromString(String src) {
            return StringToType.get(src);
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

    public static class Factory {
        public static List<Source> createSources(SourceDesc desc) {
            Type type = Source.Type.fromString(desc.getType());
            if (type == null) {
                return null;
            }
            return type.createSources(desc);
        }
    }

    protected String path;

    public Source(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public char getSeparatorChar() {
        throw new UnsupportedOperationException();
    }

    public long getSize() {
        throw new UnsupportedOperationException(
                "this method should be declared in sub-class");
    }

    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException(
                "this method should be declared in sub-class");
    }

    @Override
    public boolean equals(Object o) {
        if (! (o instanceof Source)) {
            return false;
        }

        Source other = (Source) o;
        return getPath().equals(other.getPath());
    }
}