package uk.gov.digital.ho.hocs.cms.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "cms.extract.single.complaint", havingValue = "enabled", matchIfMissing = false)
public class ExtractSingleComplaintRunner implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final ComplaintsService complaintsService;

    private final String complaintId;

    public ExtractSingleComplaintRunner(@Value("${complaint.id}") String complaintId,
                                        ApplicationContext applicationContext,
                                        ComplaintsService complaintsService) {
        this.complaintId = complaintId;
        this.applicationContext = applicationContext;
        this.complaintsService = complaintsService;
    }

    @Override
    public void run(String... args) throws SQLException {
        log.info("Extract a single complaint started");
        complaintsService.migrateComplaint(complaintId);
        System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }
}
