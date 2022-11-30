package uk.gov.digital.ho.hocs.cms.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import uk.gov.digital.ho.hocs.cms.documents.DocumentExtrator;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
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
    public void run(String... args) {
        log.info("Extract documents started");
        List<BigDecimal> cases = Collections.emptyList();
        try {
            cases = complaintsExtractor.getComplaintIdsByDateRange("2022-01-01","2022-12-30");
        } catch (SQLException e) {
            log.error("Complaints extraction failed for {} to {}", "2022-01-01","2022-12-30");
        }
        for (BigDecimal aCase : cases) {
            try {
                documentExtrator.copyDocumentsForCase(aCase.intValue());
            } catch (SQLException e) {
                log.error("Document extraction failed for complaint {}: Reason {}", aCase.intValue(), e.getMessage());
            }
        }
        log.info("Document extraction exiting");
    }

}
