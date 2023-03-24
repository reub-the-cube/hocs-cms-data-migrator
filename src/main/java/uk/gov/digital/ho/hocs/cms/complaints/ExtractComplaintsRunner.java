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
@ConditionalOnProperty(name = "cms.extract.complaints", havingValue = "enabled", matchIfMissing = false)
public class ExtractComplaintsRunner implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final ComplaintsService complaintsService;

    private final String startDate;
    private final String endDate;
    private final String complaintExtractionType;

    public ExtractComplaintsRunner(@Value("${complaint.start.date}") String startDate,
                                   @Value("${complaint.end.date}") String endDate,
                                   @Value("${complaint.extraction.type}") String complaintExtractionType,
                                   ApplicationContext applicationContext,
                                   ComplaintsService complaintsService) {
        this.complaintExtractionType = complaintExtractionType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.applicationContext = applicationContext;
        this.complaintsService = complaintsService;
    }

    @Override
    public void run(String... args) {
        log.info("Extract all complaints started for dates {} until {}", startDate, endDate);
        complaintsService.migrateComplaints(startDate, endDate, ComplaintExtractionType.ALL_CASES);
        System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }
}
