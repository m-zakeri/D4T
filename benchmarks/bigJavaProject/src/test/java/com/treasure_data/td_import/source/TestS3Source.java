package com.treasure_data.td_import.source;

import static org.junit.Assert.assertEquals;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.Test;

public class TestS3Source {
    @Test
    public void createAmazonS3Client() {
        AmazonS3 client;
        client = S3Source.createAmazonS3Client(SourceDesc
                .create("s3://kkk:sss@s3-ap-northeast-1.amazonaws.com/bucket-name/path/to/file.csv"), "ap-northeast-1");
        assertEquals("ap-northeast-1", client.getRegion().getFirstRegionId());
    }
}
