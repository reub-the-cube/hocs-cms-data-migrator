package uk.gov.digital.ho.hocs.cms.complaints;

import com.microsoft.sqlserver.jdbc.SQLServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeExceptionMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.digital.ho.hocs.cms.documents.DocumentExtrator;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "cms.extract.documents", havingValue = "enabled", matchIfMissing = false)
public class ExtractComplaintsRunner implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final DocumentExtrator documentExtrator;
    private final ComplaintsExtractor complaintsExtractor;


    public ExtractComplaintsRunner(ApplicationContext applicationContext, DocumentExtrator documentExtrator, ComplaintsExtractor complaintsExtractor) {
        this.applicationContext = applicationContext;
        this.documentExtrator = documentExtrator;
        this.complaintsExtractor = complaintsExtractor;
    }

    @Override
    public void run(String... args) throws SQLException {
        log.info("Extract documents started");
        List<BigDecimal> complaints = complaintsExtractor.getComplaintIdsByDateRange("2022-01-01","2022-12-30");
        for (BigDecimal complaint : complaints) {
                documentExtrator.copyDocumentsForCase(complaint.intValue());
            }
        log.info("Complaints extraction between dates {} and {} finished.","2022-01-01","2022-12-30");
    }

    @Bean
    public ExitCodeExceptionMapper exceptionBasedExitCode() {
        return exception -> {
            if (exception.getCause() instanceof SQLServerException) {
                SQLServerException sqlServerException = (SQLServerException) exception.getCause();
                log.error("SQL Server exception: {}, SQL Server state: {}", sqlServerException.getMessage(), sqlServerException.getSQLState());
                return 2;
            }
            return 99;
        };
    }
}
