package uk.gov.digital.ho.hocs.cms.documents;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.cms.client.DocumentS3Client;
import uk.gov.digital.ho.hocs.cms.domain.DocumentExtractRecord;

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
public class DocumentExtrator {

    private final DataSource dataSource;
    private final DocumentS3Client documentS3Client;

    public DocumentExtrator(DataSource dataSource, DocumentS3Client documentS3Client) {
        this.dataSource = dataSource;
        this.documentS3Client = documentS3Client;
    }

    public void getDocumentsForCase() {
        String documentsForCase = """
        SELECT '-' AS LinkedDocResults, dst.* FROM lgncc_logEvents lev 
        inner join lgncc_noteAttachments nat on nat.noteId = lev.LogEventID 
        inner join LGNCC_DOCUMENTSTORE dst on dst.id = nat.reference 
        where lev.CaseId = ?
        """;
    }

    public DocumentExtractRecord getDocument(int documentId) throws SQLException {
        DocumentExtractRecord cdr = new DocumentExtractRecord();
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
