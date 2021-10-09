//
//Treasure Data Bulk-Import Tool in Java
//
//Copyright (C) 2012 - 2013 Muga Nishizawa
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package com.treasure_data.td_import.source;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalFileSource extends Source {
    private static final Logger LOG = Logger.getLogger(LocalFileSource.class.getName());

    public static List<Source> createSources(SourceDesc desc) {
        // The following codes are not used on windows.

        String rawPath = File.separator + desc.getPath();

        List<File> files = getSources(rawPath);
        List<Source> srcs = new ArrayList<Source>();
        for (File file : files) {
            LOG.info(String.format("create local-src file=%s, rawPath=%s",
                    file.getAbsolutePath(), rawPath));
            srcs.add(new LocalFileSource(file.getAbsolutePath()));
        }
        return srcs;
    }

    static List<File> getSources(String basePath) {
        int index = basePath.indexOf('*');
        if (index < 0) {
            return Arrays.asList(new File(basePath));
        }

        //  The following codes are never executed.

        String prefix;
        String firstStar = basePath.substring(0, index);
        int lastPathSep = firstStar.lastIndexOf(File.separatorChar);
        if (lastPathSep < 0) {
            prefix = firstStar;
        } else {
            prefix = firstStar.substring(0, lastPathSep);
        }

        LOG.info(String.format("find local files: basePath=%s, prefix=%s",
                basePath, prefix));

        File findDir = new File(prefix);
        File[] files = findDir.listFiles();

        return filterSources(Arrays.asList(files), basePath);
    }

    static List<File> filterSources(List<File> files, String basePath) {
        String regex = basePath.replace("*", "([^\\s]*)");
        Pattern pattern = Pattern.compile(regex);

        LOG.info(String.format("regex matching: regex=%s", regex));
        List<File> matched = new ArrayList<File>();
        for (File file : files) {
            Matcher m = pattern.matcher(file.getAbsolutePath());
            if (m.matches()) {
                matched.add(file);
            }
        }
        return matched;
    }

    private File file;

    public LocalFileSource(String fileName) {
        super(fileName);
        this.file = new File(fileName);
    }

    @Override
    public char getSeparatorChar() {
        return File.separatorChar;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new BufferedInputStream(new FileInputStream(getFileName()));
    }

    @Override
    public String getPath() {
        return file.getPath();
    }

    public String getFileName() {
        return getPath();
    }

    public File getFile() {
        return file;
    }

    @Override
    public long getSize() {
        return file.length();
    }

    @Override
    public String toString() {
        return String.format("local-src(path=%s)", getFileName());
    }
}
