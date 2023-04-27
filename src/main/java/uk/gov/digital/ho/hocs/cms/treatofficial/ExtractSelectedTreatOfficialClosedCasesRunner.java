package uk.gov.digital.ho.hocs.cms.treatofficial;

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
@ConditionalOnProperty(name = "cms.extract.selected.treat.official.closed.cases", havingValue = "enabled", matchIfMissing = false)
public class ExtractSelectedTreatOfficialClosedCasesRunner implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final TreatOfficialService treatOfficialService;

    private final String complaintIds;

    public ExtractSelectedTreatOfficialClosedCasesRunner(@Value("${treat.official.ids}") String complaintIds,
                                                         ApplicationContext applicationContext,
                                                         TreatOfficialService treatOfficialService) {
        this.complaintIds = complaintIds;
        this.applicationContext = applicationContext;
        this.treatOfficialService = treatOfficialService;
    }

    @Override
    public void run(String... args) {
        log.info("Extract treat official cases ", complaintIds);
        List<String> complaintIds = getComplaintIds(this.complaintIds);
        treatOfficialService.migrateTreatOfficials(complaintIds);
        System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }

    private List<String> getComplaintIds(String complaintIds){
        return Arrays.asList(complaintIds.split("\\|"));
    }

}
