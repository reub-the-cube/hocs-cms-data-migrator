package uk.gov.digital.ho.hocs.cms.documents;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.cms.client.DocumentS3Client;
import uk.gov.digital.ho.hocs.cms.domain.DocumentExtractRecord;
import uk.gov.digital.ho.hocs.cms.domain.repository.DocumentsRepository;
import uk.gov.digital.ho.hocs.cms.message.CaseAttachment;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public List<CaseAttachment> copyDocumentsForCase(int caseId) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(DOCUMENTS_FOR_CASE);
        ps.setInt(1, caseId);
        ResultSet rs = ps.executeQuery();
        List<CaseAttachment> attachments = new ArrayList<>();
        while (rs.next()) {
            BigDecimal documentId = rs.getBigDecimal(1);
            CaseAttachment attachment = getDocument(documentId.intValue(), caseId);
            if (attachment.getDocumentPath() != null) {
                attachments.add(attachment);
            } else {
                log.error("Document ID {} failed to extract", documentId.intValue());
            }
        }
        // Close resources unless SQLException is thrown which will terminate the application.
        ps.close();
        conn.close();
        return attachments;
    }

    private CaseAttachment getDocument(int documentId, int caseId) throws SQLException {
        DocumentExtractRecord record = new DocumentExtractRecord();
        record.setDocumentId(documentId);
        record.setCaseId(caseId);
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(GET_DOCUMENT);
        ps.setInt(1, documentId);
        ResultSet res = ps.executeQuery();
        Map<String, String> result = new HashMap<>();
        CaseAttachment caseAttachment = new CaseAttachment();
        if (res.next()) {
            int id = res.getInt(1);
            String fileName = res.getString(2);
            caseAttachment.setDisplayName(fileName);
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
        ps.close();
        conn.close();
        if (result.containsKey(DocumentS3Client.RESULT.PASS.label)) {
            record.setDocumentExtracted(true);
            String fileName = result.get(DocumentS3Client.RESULT.PASS.label);
            record.setTempFileName(fileName);
            saveDocumentResult(record);
            caseAttachment.setDocumentPath(fileName);
        } else {
            record.setDocumentExtracted(false);
            record.setFailureReason(result.get(DocumentS3Client.RESULT.FAIL.label));
            saveDocumentResult(record);
        }
        return caseAttachment;
    }
    @Transactional
    void saveDocumentResult(DocumentExtractRecord record) {
        documentsRepository.save(record);
    }

}
