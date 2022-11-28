package uk.gov.digital.ho.hocs.cms.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import uk.gov.digital.ho.hocs.cms.documents.DocumentExtrator;
import uk.gov.digital.ho.hocs.cms.domain.DocumentExtractRecord;
import uk.gov.digital.ho.hocs.cms.domain.repository.DocumentsRepository;

import java.math.BigDecimal;
import java.sql.SQLException;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "cms.extract.documents", havingValue = "enabled", matchIfMissing = false)
public class ExtractComplaintsRunner implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final DocumentsRepository documentsRepository;
    private final DocumentExtrator extractDocuments;
    private final ComplaintsExtractor complaintsExtractor;


    public ExtractComplaintsRunner(ApplicationContext applicationContext, DocumentsRepository documentsRepository, DocumentExtrator documentExtrator, ComplaintsExtractor complaintsExtractor) {
        this.applicationContext = applicationContext;
        this.documentsRepository = documentsRepository;
        this.extractDocuments = documentExtrator;
        this.complaintsExtractor = complaintsExtractor;
    }

    @Override
    public void run(String... args) {
        log.info("Extract documents started");
        DocumentExtractRecord cdr = new DocumentExtractRecord();
        cdr.setCaseId(BigDecimal.valueOf(1000000));
        try {
            complaintsExtractor.getCaseIdsByDateRange("jan 25,2021","jan 25,2022");
            DocumentExtractRecord res = extractDocuments.getDocument(1000000);
            documentsRepository.save(res);
        } catch (SQLException e) {
            log.error("Document {} extraction failed: Reason {}", e.getMessage());
            cdr.setFailureReason(e.getMessage());
            documentsRepository.save(cdr);
        }
        log.info("Document extraction completed successfully, exiting");

    }

}
