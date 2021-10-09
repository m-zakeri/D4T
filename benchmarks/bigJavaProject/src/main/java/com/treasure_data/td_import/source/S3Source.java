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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.treasure_data.td_import.Configuration;

/**
 * td import:prepare "s3://access_key_id:secret_access_key@/bucket/key_prefix"
 */
public class S3Source extends Source {
    private static final Logger LOG = Logger.getLogger(S3Source.class.getName());

    public static List<Source> createSources(SourceDesc desc) {
        String rawPath = desc.getPath();
        String[] elm = rawPath.split("/");

        String bucket = elm[0];
        String basePath = rawPath.substring(bucket.length() + 1, rawPath.length());

        System.setProperty("com.amazonaws.sdk.disableCertChecking", "1");

        String region = "";
        try {
            region = getBucketLocation(desc, bucket);
        } catch (AmazonServiceException e) {
            LOG.warning("Failed to get S3 bucket location. Please make sure that your account is given 's3:GetBucketLocation' role.");
            throw e;
        }
        AmazonS3 client = createAmazonS3Client(desc, region);
        List<S3ObjectSummary> s3objects = getSources(client, bucket, basePath);
        List<Source> srcs = new ArrayList<Source>();
        for (S3ObjectSummary s3object : s3objects) {
            LOG.info(String.format("create s3-src s3object=%s, rawPath=%s",
                    s3object.getKey(), rawPath));
            srcs.add(new S3Source(createAmazonS3Client(desc, region), rawPath,
                    s3object.getBucketName(), s3object.getKey(), s3object.getSize()));
        }

        return srcs;
    }

    static AmazonS3 createAmazonS3Client(SourceDesc desc, String region) {
        return createClientBuilder(desc).withRegion(region).build();
    }

    static AmazonS3ClientBuilder createClientBuilder(SourceDesc desc) {
        String accessKey = desc.getUser();
        if (accessKey == null || accessKey.isEmpty()) {
            throw new IllegalArgumentException("S3 AccessKey is null or empty.");
        }
        String secretAccessKey = desc.getPassword();
        if (secretAccessKey == null || secretAccessKey.isEmpty()) {
            throw new IllegalArgumentException("S3 SecretAccessKey is null or empty.");
        }
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretAccessKey);

        ClientConfiguration clientConfig = new ClientConfiguration()
                .withProtocol(Configuration.BI_PREPARE_S3_PROTOCOL)
                .withMaxConnections(Configuration.BI_PREPARE_S3_MAX_CONNECTIONS)
                .withMaxErrorRetry(Configuration.BI_PREPARE_S3_MAX_ERRORRETRY)
                .withSocketTimeout(Configuration.BI_PREPARE_S3_SOCKET_TIMEOUT);

        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withClientConfiguration(clientConfig);
    }

    static String getBucketLocation(SourceDesc desc, String bucket) {
        AmazonS3 client = createClientBuilder(desc).build();
        String bucketRegion = client.getBucketLocation(bucket);
        Region region = Region.fromValue(bucketRegion);
        if (region.equals(Region.US_Standard)) {
            return "us-east-1";
        } else {
            return region.toString();
        }
    }

    static List<S3ObjectSummary> getSources(AmazonS3 client, String bucket, String basePath) {
        String prefix;
        int index = basePath.indexOf('*');
        if (index >= 0) {
            prefix = basePath.substring(0, index);
        } else {
            ObjectMetadata om = client.getObjectMetadata(bucket, basePath);
            S3ObjectSummary s3object = new S3ObjectSummary();
            s3object.setBucketName(bucket);
            s3object.setKey(basePath);
            s3object.setSize(om.getContentLength());

            return Arrays.asList(s3object);
        }

        LOG.info(String.format("list s3 files by client %s: bucket=%s, basePath=%s, prefix=%s",
                client, bucket, basePath, prefix));

        List<S3ObjectSummary> s3objects = new ArrayList<S3ObjectSummary>();
        String lastKey = prefix;
        do {
            ObjectListing listing = client.listObjects(new ListObjectsRequest(
                    bucket, prefix, lastKey, null, 1024));
            for(S3ObjectSummary s3object : listing.getObjectSummaries()) {
                s3objects.add(s3object);
            }
            lastKey = listing.getNextMarker();
        } while (lastKey != null);

        return filterSources(s3objects, basePath);
    }

    static List<S3ObjectSummary> filterSources(List<S3ObjectSummary> s3objects, String basePath) {
        String regex = basePath.replace("*", "([^\\s]*)");
        Pattern pattern = Pattern.compile(regex);

        LOG.info(String.format("regex matching: regex=%s", regex));

        List<S3ObjectSummary> matched = new ArrayList<S3ObjectSummary>();
        for (S3ObjectSummary s3object : s3objects) {
            Matcher m = pattern.matcher(s3object.getKey());
            if (m.matches()) {
                matched.add(s3object);
            }
        }
        return matched;
    }

    protected AmazonS3 client;

    protected String bucket;
    protected String key;
    protected long size;
    protected String rawPath;

    S3Source(AmazonS3 client, String rawPath, String bucket, String key, long size) {
        super("s3://"+bucket+"/"+key);
        this.client = client;
        this.bucket = bucket;
        this.key = key;
        this.size = size;
        this.rawPath = rawPath;
    }

    @Override
    public char getSeparatorChar() {
        return '/';
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        LOG.info(String.format("get s3 file by client %s: bucket=%s, key=%s",
                client, bucket, key));
        GetObjectRequest req = new GetObjectRequest(bucket, key);
        req.setRange(0, size);
        S3Object object = client.getObject(req);

        if (object != null) {
            return object.getObjectContent();
        } else {
            throw new IOException("s3 file is null.");
        }
    }

    @Override
    public String toString() {
        return String.format("s3-src(bucket=%s,path=%s)", bucket, path);
    }
}
