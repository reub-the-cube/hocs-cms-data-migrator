package uk.gov.digital.ho.hocs.cms.aws;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.cloud.aws.messaging.config.annotation.EnableSqs;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@EnableSqs
@Configuration
@Profile("localstack")
@ConditionalOnProperty(prefix = "aws.sqs", value = "enabled", havingValue = "true")
public class LocalStackConfiguration {

    private final AWSCredentialsProvider awsCredentialsProvider;
    private final AwsClientBuilder.EndpointConfiguration endpoint;
    public LocalStackConfiguration(@Value("${localstack.base-url}") String baseUrl,
                                   @Value("${localstack.config.region}") String region) {
        this.awsCredentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials("test", "test"));
        this.endpoint = new AwsClientBuilder.EndpointConfiguration(baseUrl, region);
    }

    @Primary
    @Bean
    public AmazonSQSAsync awsSqsClient(
            @Value("${aws.sqs.url}") String awsBaseUrl,
            @Value("${aws.sqs.access-key}") String accessKey,
            @Value("${aws.sqs.secret-key}") String secretKey,
            @Value("${aws.region}") String region){
        return AmazonSQSAsyncClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(accessKey, secretKey)))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(awsBaseUrl, region))
                .build();
    }

    @Primary
    @Bean
    public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory(AmazonSQSAsync amazonSqs) {
        SimpleMessageListenerContainerFactory factory = new SimpleMessageListenerContainerFactory();

        factory.setAmazonSqs(amazonSqs);
        factory.setMaxNumberOfMessages(10);
        factory.setWaitTimeOut(5);

        return factory;
    }

    @Primary
    @Bean
    public AmazonS3 s3Client() {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(awsCredentialsProvider)
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(endpoint)
                .disableChunkedEncoding()
                .build();
    }
}
