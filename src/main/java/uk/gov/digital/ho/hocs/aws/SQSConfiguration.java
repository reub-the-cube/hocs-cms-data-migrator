package uk.gov.digital.ho.hocs.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;

@Configuration
@Profile({"sqs"})
public class SQSConfiguration {

    @Bean
    public AmazonSQSAsync sqsClient(@Value("${aws.sqs.access-key}") String accessKey,
                               @Value("${aws.sqs.secret-key}") String secretKey,
                               @Value("${aws.sqs.region}") String region) {

        if (!StringUtils.hasText(accessKey)) {
            throw new BeanCreationException("Failed to create SQS client bean. Need non-blank value for access key");
        }

        if (!StringUtils.hasText(secretKey)) {
            throw new BeanCreationException("Failed to create SQS client bean. Need non-blank values for secret key");
        }

        if (!StringUtils.hasText(region)) {
            throw new BeanCreationException("Failed to create SQS client bean. Need non-blank values for region: " + region);
        }

        return AmazonSQSAsyncClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .withClientConfiguration(new ClientConfiguration())
                .build();
    }
}
