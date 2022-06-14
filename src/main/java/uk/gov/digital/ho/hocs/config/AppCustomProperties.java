package uk.gov.digital.ho.hocs.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotBlank;

@Configuration
@ConfigurationProperties(prefix = "run.config")
@Getter
@Setter
public class AppCustomProperties {
    @NotBlank
    private String queueName;
    @NotBlank
    private Integer numMessages;

    private String complaintType;
}
