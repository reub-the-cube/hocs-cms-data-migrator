package uk.gov.digital.ho.hocs.cms.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "cms.extract.multiple.complaints", havingValue = "enabled", matchIfMissing = false)
public class ExtractSelectedComplaintsRunner implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final ComplaintsService complaintsService;

    private final String complaintIds;

    public ExtractSelectedComplaintsRunner(@Value("${complaint.ids}") String complaintIds,
                                        ApplicationContext applicationContext,
                                        ComplaintsService complaintsService) {
        this.complaintIds = complaintIds;
        this.applicationContext = applicationContext;
        this.complaintsService = complaintsService;
    }

    @Override
    public void run(String... args) {
        log.info("List of complaint ids from the environment {}", complaintIds);

        List<String> complaintIdList = getComplaintIds(complaintIds);

        for (String complaintId: complaintIdList) {
            log.info("Extract a single complaint started for complaint ID {}", complaintId);
            complaintsService.migrateComplaint(complaintId);
        }

        System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }

    private List<String> getComplaintIds(String complaintIds){
        return Arrays.asList(complaintIds.split("\\|"));
    }

}
