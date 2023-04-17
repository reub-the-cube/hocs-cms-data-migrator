package uk.gov.digital.ho.hocs.cms.treatofficial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "cms.extract.treat.official.cases", havingValue = "enabled", matchIfMissing = false)
public class ExtractTreatOfficialCasesRunner implements CommandLineRunner {

    private final String startDate;
    private final String endDate;
    private final ApplicationContext applicationContext;
    private final TreatOfficialService treatOfficialService;

    public ExtractTreatOfficialCasesRunner(@Value("${complaint.start.date}") String startDate,
                                           @Value("${complaint.end.date}") String endDate,
                                           ApplicationContext applicationContext,
                                           TreatOfficialService treatOfficialService) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.applicationContext = applicationContext;
        this.treatOfficialService = treatOfficialService;
    }

    @Override
    public void run(String... args) {
        log.info("Extract treat official cases started for dates {} until {}", startDate, endDate);
        treatOfficialService.migrateTreatOfficials(startDate, endDate);
        System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }
}
