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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.treasure_data.td_import.Configuration;

/**
 * 
 * td import:prepare mysql://user:password@host/database/table 
 */
public class MysqlSource extends Source {
    private static final Logger LOG = Logger.getLogger(MysqlSource.class.getName());

    public static List<Source> createSources(SourceDesc desc) {
        String path = desc.toString();
        String user = desc.getUser();
        String password = desc.getPassword();
        String host = desc.getHost();
        int port = desc.getPort(3306);
        String[] elements = desc.getPath().split("/");
        String database = elements[0];
        String table = elements[1];
        // TODO need validation for each variables

        LOG.info(String.format("create mysql-src rawPath=%s", path));
        return Arrays.asList((Source) new MysqlSource(path, user, password, host,
                port, database, table));
    }

    protected String user;
    protected String password;
    protected String host;
    protected int port;
    protected String database;
    protected String table;

    MysqlSource(String path, String user, String password, String host, int port,
            String database, String table) {
        super(path);
    }

    @Override
    public long getSize() {
        return 0L;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null; // TODO
    }

    @Override
    public String toString() {
        return String.format("mysql-src(path=%s)", path);
    }
}
