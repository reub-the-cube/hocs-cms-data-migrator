package uk.gov.digital.ho.hocs.cms.document;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.digital.ho.hocs.cms.client.DocumentS3Client;

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

@Configuration
@Slf4j
@ConditionalOnProperty(name = "cms.extract.documents", havingValue = "enabled", matchIfMissing = false)
public class ExtractDocumentsRunner implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final DocumentS3Client documentS3Client;

    public ExtractDocumentsRunner(ApplicationContext applicationContext, DataSource dataSource, JdbcTemplate jdbcTemplate, DocumentS3Client documentS3Client) {
        this.applicationContext = applicationContext;
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
        this.documentS3Client = documentS3Client;
    }

    @Override
    public void run(String... args) throws SQLException, IOException {
        log.info("Extract documents started");
        Connection connection = dataSource.getConnection();
        String query = "select * from LGNCC_DOCUMENTSTORE where id = 1000000;";
        PreparedStatement stmt = connection.prepareStatement(query);
        ResultSet res = stmt.executeQuery();
        int rowCount = 0;
        while (res.next()) {
            int id = res.getInt(1);
            String fileName = res.getString(2);
            InputStream is = res.getBinaryStream(3);
            byte[] bytes = IOUtils.toByteArray(is);
            rowCount++;
            //writeFile(is);
            documentS3Client.storeUntrustedDocument(fileName, bytes, id);
        }
        System.out.println("Number of records : " + rowCount);
        log.info("Document extraction completed successfully, exiting");
    }

    private void writeFile(InputStream is) throws IOException {
        File targetFile = new File("/Users/rjweeks/Downloads/outputFile.pdf");
        Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
