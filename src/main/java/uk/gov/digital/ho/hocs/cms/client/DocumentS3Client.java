package uk.gov.digital.ho.hocs.cms.client;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.SSEAwsKeyManagementParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent.DOCUMENT_COPY_FAILED;

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
                            @Value("${aws.s3.untrusted.account.kms-key}") String bucketKmsKey) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.bucketKmsKey = bucketKmsKey;
    }

    public String storeUntrustedDocument(String originalFilename, byte[] bytes, BigDecimal id) {
        ObjectMetadata metaData = buildObjectMetadata(originalFilename, bytes.length, id);
        String tempObjectName = getTempObjectName();
        try {
            PutObjectRequest uploadRequest = buildPutObjectRequest(bytes, metaData, tempObjectName);
            s3Client.putObject(uploadRequest);
            log.info("S3 Put Object success. ID = {}", id);
        }
        catch (SdkClientException e) {
            log.error("S3 PutObject failure. Reason: {}, ID = {}", e.getMessage(), id);
            throw new ApplicationExceptions.ExtractDocumentException(
                    String.format("Failed to put document ID: " + id), DOCUMENT_COPY_FAILED);
        }
        return tempObjectName;
    }

    PutObjectRequest buildPutObjectRequest(byte[] bytes, ObjectMetadata metaData, String tempObjectName) {
        PutObjectRequest uploadRequest = new PutObjectRequest(bucketName, tempObjectName,
                new ByteArrayInputStream(bytes), metaData);

        if (StringUtils.hasText(bucketKmsKey)) { // Will be empty when running local. Workaround because localstack doesn't use HTTPS
            uploadRequest = uploadRequest.withSSEAwsKeyManagementParams(new SSEAwsKeyManagementParams(bucketKmsKey));
        }

        return uploadRequest;
    }

    String getTempObjectName() {
        return UUID.randomUUID().toString();
    }

    ObjectMetadata buildObjectMetadata(String originalFilename, int length, BigDecimal id) {
        ObjectMetadata metaData = new ObjectMetadata();
        metaData.setContentType("application/octet-stream");
        metaData.addUserMetadata(META_DATA_LABEL, originalFilename);
        metaData.addUserMetadata(META_DATA_ID, String.valueOf(id));
        metaData.setContentLength(length);
        return metaData;
    }
}
