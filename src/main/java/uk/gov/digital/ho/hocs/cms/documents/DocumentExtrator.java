package uk.gov.digital.ho.hocs.cms.documents;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.cms.client.DocumentS3Client;
import uk.gov.digital.ho.hocs.cms.domain.DocumentExtractRecord;
import uk.gov.digital.ho.hocs.cms.domain.repository.DocumentsRepository;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
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
    private final DocumentsRepository documentsRepository;

    private static final String GET_DOCUMENT = "select * from LGNCC_DOCUMENTSTORE where id = ?;";

    private static final String DOCUMENTS_FOR_CASE = """
        SELECT dst.* FROM lgncc_logEvents lev 
        inner join lgncc_noteAttachments nat on nat.noteId = lev.LogEventID 
        inner join LGNCC_DOCUMENTSTORE dst on dst.id = nat.reference 
        where lev.CaseId = ?
        """;

    public DocumentExtrator(DataSource dataSource, DocumentS3Client documentS3Client, DocumentsRepository documentsRepository) {
        this.dataSource = dataSource;
        this.documentS3Client = documentS3Client;
        this.documentsRepository = documentsRepository;
    }

    public void copyDocumentsForCase(int caseId) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement stmt = connection.prepareStatement(DOCUMENTS_FOR_CASE);
        stmt.setInt(1, caseId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            BigDecimal documentId = rs.getBigDecimal(1);
            getDocument(documentId.intValue(), caseId);
        }
    }

    private void getDocument(int documentId, int caseId) throws SQLException {
        DocumentExtractRecord record = new DocumentExtractRecord();
        record.setDocumentId(documentId);
        record.setCaseId(caseId);
        Connection connection = dataSource.getConnection();
        PreparedStatement stmt = connection.prepareStatement(GET_DOCUMENT);
        stmt.setInt(1, documentId);
        ResultSet res = stmt.executeQuery();
        String result ="";
        if (res.next()) {
            int id = res.getInt(1);
            String fileName = res.getString(2);
            InputStream is = res.getBinaryStream(3);
            try {
                byte[] bytes = IOUtils.toByteArray(is);
                result = documentS3Client.storeUntrustedDocument(fileName, bytes, id);
            } catch (IOException e) {
                log.error("Error converting document to byte array {}", id);
                record.setFailureReason(e.getMessage());
            }
        } else {
            log.error("No document found for ID {}", documentId);
        }
        stmt.close();
        if (isValidUUID(result)) {
            record.setDocumentExtracted(true);
            record.setTempFileName(result);
            documentsRepository.save(record);
        } else {
            record.setDocumentExtracted(false);
            record.setFailureReason(result);
            documentsRepository.save(record);
        }
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
