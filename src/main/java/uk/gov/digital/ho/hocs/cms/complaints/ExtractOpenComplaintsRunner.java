package uk.gov.digital.ho.hocs.cms.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "cms.extract.open.complaints", havingValue = "enabled", matchIfMissing = false)
public class ExtractOpenComplaintsRunner implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final ComplaintsService complaintsService;

    private final String startDate;
    private final String endDate;

    public ExtractOpenComplaintsRunner(@Value("${complaint.start.date}") String startDate,
                                   @Value("${complaint.end.date}") String endDate,
                                   ApplicationContext applicationContext,
                                   ComplaintsService complaintsService) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.applicationContext = applicationContext;
        this.complaintsService = complaintsService;
    }

    @Override
    public void run(String... args) {
        log.info("Extract open complaints started for dates {} until {}", startDate, endDate);
        complaintsService.migrateComplaints(startDate, endDate, ComplaintExtractionType.OPEN_CASES_ONLY);
        System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }
}
