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
import uk.gov.digital.ho.hocs.cms.documents.DocumentExtrator;
import uk.gov.digital.ho.hocs.cms.domain.ComplaintExtractRecord;
import uk.gov.digital.ho.hocs.cms.domain.repository.ComplaintsRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "cms.extract.documents", havingValue = "enabled", matchIfMissing = false)
public class ExtractComplaintsRunner implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final ComplaintsService complaintsService;


    public ExtractComplaintsRunner(ApplicationContext applicationContext, ComplaintsService complaintsService) {
        this.applicationContext = applicationContext;
        this.complaintsService = complaintsService;

    }

    @Override
    public void run(String... args) throws SQLException {
        log.info("Extract documents started");
        if (args.length != 2) {
            log.error("Need to supply date range for extraction");
            System.exit(SpringApplication.exit(applicationContext, () -> 0));
        }
        complaintsService.migrate(args[0], args[1]);

        System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }

    @Bean
    public ExitCodeExceptionMapper exceptionBasedExitCode() {
        return exception -> {
            if (exception.getCause() instanceof SQLServerException sqlServerException) {
                log.error("SQL Server exception: {}, SQL Server state: {}", sqlServerException.getMessage(), sqlServerException.getSQLState());
                return 2;
            }
            return 99;
        };
    }



}
