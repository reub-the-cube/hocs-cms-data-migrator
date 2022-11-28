package uk.gov.digital.ho.hocs.cms.documents;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.cms.client.DocumentS3Client;
import uk.gov.digital.ho.hocs.cms.domain.CaseDocumentRecord;
import uk.gov.digital.ho.hocs.cms.domain.repository.DocumentsRepository;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ExtractDocuments {

    private final DataSource dataSource;
    private final DocumentS3Client documentS3Client;

    public ExtractDocuments(DataSource dataSource, DocumentS3Client documentS3Client) {
        this.dataSource = dataSource;
        this.documentS3Client = documentS3Client;
    }

    public CaseDocumentRecord getDocument(int documentId) throws SQLException {
        CaseDocumentRecord cdr = new CaseDocumentRecord();
        cdr.setDocumentId(documentId);
        Connection connection = dataSource.getConnection();
        String query = "select * from LGNCC_DOCUMENTSTORE where id = ?;";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, documentId);
        ResultSet res = stmt.executeQuery();
        //stmt.close();
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

    private final static Pattern UUID_REGEX_PATTERN =
            Pattern.compile("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$");

    public static boolean isValidUUID(String str) {
        if (str == null) {
            return false;
        }
        return UUID_REGEX_PATTERN.matcher(str).matches();
    }
}
