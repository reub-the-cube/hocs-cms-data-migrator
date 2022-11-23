package uk.gov.digital.ho.hocs.cms.client;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@Slf4j
public class DocumentS3Client {

    private static final String META_DATA_LABEL = "originalName";
    private static final String META_DATA_ID = "id";

    private final AmazonS3 s3Client;
    private final String bucketName;
    private final String bucketKmsKey;

    public DocumentS3Client(AmazonS3 s3Client,
                            @Value("${aws.s3.untrusted.bucket-name}") String bucketName,
                            @Value("${aws.s3.untrusted.account.bucket-kms-key}") String bucketKmsKey) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.bucketKmsKey = bucketKmsKey;
    }

    public String storeUntrustedDocument(String originalFilename, byte[] bytes, int id) throws IOException {
        ObjectMetadata metaData = buildObjectMetadata(originalFilename, bytes.length, id);
        String tempObjectName = getTempObjectName();
        s3Client.putObject(bucketName, tempObjectName, new ByteArrayInputStream(bytes), metaData);
        return tempObjectName;
    }

    String getTempObjectName() {
        return UUID.randomUUID().toString();
    }

    ObjectMetadata buildObjectMetadata(String originalFilename, int length, int id) {
        ObjectMetadata metaData = new ObjectMetadata();
        metaData.setContentType("application/octet-stream");
        metaData.addUserMetadata(META_DATA_LABEL, originalFilename);
        metaData.addUserMetadata(META_DATA_ID, String.valueOf(id));
        metaData.setContentLength(length);
        return metaData;
    }
}
