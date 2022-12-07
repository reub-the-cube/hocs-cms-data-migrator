package uk.gov.digital.ho.hocs.cms.complaints;

import com.microsoft.sqlserver.jdbc.SQLServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeExceptionMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "cms.extract.complaints", havingValue = "enabled", matchIfMissing = false)
public class ExtractComplaintsRunner implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final ComplaintsService complaintsService;

    public ExtractComplaintsRunner(ApplicationContext applicationContext,
                                   ComplaintsService complaintsService) {
        this.applicationContext = applicationContext;
        this.complaintsService = complaintsService;
    }

    @Override
    public void run(String... args) throws SQLException {
        log.info("Extract complaints started");
        complaintsService.migrateComplaints();
        System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }
}
