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
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final DocumentS3Client documentS3Client;
    private final DocumentsRepository documentsRepository;

    public ExtractDocumentsRunner(ApplicationContext applicationContext, @Qualifier("cmsDatasource") DataSource dataSource,
                                  JdbcTemplate jdbcTemplate, DocumentS3Client documentS3Client, DocumentsRepository documentsRepository) {
        this.applicationContext = applicationContext;
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.documentS3Client = documentS3Client;
        this.documentsRepository = documentsRepository;
    }

    @Override
    public void run(String... args) {
        log.info("Extract documents started");
        try {
            CaseDocumentRecord res = getDocument(1000000);
            documentsRepository.save(res);
        } catch (SQLException e) {
            log.error("Document {} extraction failed: Reason {}", e.getMessage());
        }
        log.info("Document extraction completed successfully, exiting");

    }

    private CaseDocumentRecord getDocument(int documentId) throws SQLException {
        CaseDocumentRecord cdr = new CaseDocumentRecord();
        cdr.setDocumentId(documentId);
        Connection connection = dataSource.getConnection();
        String query = "select * from LGNCC_DOCUMENTSTORE where id = ?;";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, documentId);
        ResultSet res = stmt.executeQuery();
        String result ="";
        while (res.next()) {
            int id = res.getInt(1);
            String fileName = res.getString(2);
            InputStream is = res.getBinaryStream(3);
            byte[] bytes = new byte[0];
            try {
                bytes = IOUtils.toByteArray(is);
            } catch (IOException e) {
                log.error("Error converting document to byte array {}", id);
                cdr.setFailureReason(e.getMessage());
                return cdr;
            }
            result = documentS3Client.storeUntrustedDocument(fileName, bytes, id);
        }
        if (isValidUUID(result)) {
            cdr.setDocumentExtracted(true);
            cdr.setTempFileName(result);
        }
        else {
            cdr.setDocumentExtracted(false);
            cdr.setFailureReason(result);
        }
        return cdr;
    }

    private void writeFile(InputStream is) throws IOException {
        File targetFile = new File("/Users/rjweeks/Downloads/outputFile.pdf");
        Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private final static Pattern UUID_REGEX_PATTERN =
            Pattern.compile("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$");

    public static boolean isValidUUID(String str) {
        if (str == null) {
            return false;
        }
        return UUID_REGEX_PATTERN.matcher(str).matches();
    }
}
