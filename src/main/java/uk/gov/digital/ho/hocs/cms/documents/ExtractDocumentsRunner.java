package uk.gov.digital.ho.hocs.cms.documents;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.digital.ho.hocs.cms.domain.CaseDocumentRecord;
import uk.gov.digital.ho.hocs.cms.client.DocumentS3Client;
import uk.gov.digital.ho.hocs.cms.domain.repository.DocumentsRepository;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "cms.extract.documents", havingValue = "enabled", matchIfMissing = false)
public class ExtractDocumentsRunner implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final DocumentsRepository documentsRepository;
    private final ExtractDocuments extractDocuments;


    public ExtractDocumentsRunner(ApplicationContext applicationContext, DocumentsRepository documentsRepository, ExtractDocuments extractDocuments) {
        this.applicationContext = applicationContext;
        this.documentsRepository = documentsRepository;
        this.extractDocuments = extractDocuments;
    }

    @Override
    public void run(String... args) {
        log.info("Extract documents started");
        CaseDocumentRecord cdr = new CaseDocumentRecord();
        cdr.setCaseId(1000000);
        try {
            CaseDocumentRecord res = extractDocuments.getDocument(1000000);
            documentsRepository.save(res);
        } catch (SQLException e) {
            log.error("Document {} extraction failed: Reason {}", e.getMessage());
            cdr.setFailureReason(e.getMessage());
            documentsRepository.save(cdr);
        }
        log.info("Document extraction completed successfully, exiting");

    }

}
