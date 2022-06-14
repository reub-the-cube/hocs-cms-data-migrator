package uk.gov.digital.ho.hocs.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;

@Configuration
@ConfigurationProperties(prefix = "aws")
@Getter
@Setter
public class AWSCustomConfig {

    @NotBlank
    private SQSCustomProperties sqs;
    @NotBlank
    private String localHost;

    @Getter
    @Setter
    private static class SQSCustomProperties {
        @NotBlank
        private String region;
        @NotBlank
        private String accessKey;
        @NotBlank
        private String secretKey;
    }

}
