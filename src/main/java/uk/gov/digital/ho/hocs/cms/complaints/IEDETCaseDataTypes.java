package uk.gov.digital.ho.hocs.cms.complaints;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.cms.util.JsonPropertySourceFactory;

import java.util.LinkedHashMap;

@Component
@PropertySource(value = "classpath:iedet-case-data-map.json", factory = JsonPropertySourceFactory.class)
@ConfigurationProperties
@Getter
@Setter
public class IEDETCaseDataTypes {

    private LinkedHashMap<String, String> categories;
}
