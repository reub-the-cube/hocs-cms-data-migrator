package uk.gov.digital.ho.hocs.cms.documents;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.cms.client.DocumentS3Client;
import uk.gov.digital.ho.hocs.cms.domain.DocumentExtractRecord;
import uk.gov.digital.ho.hocs.cms.domain.repository.DocumentsRepository;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseAttachment;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent.DOCUMENT_BYTE_CONVERSION_FAILED;
import static uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent.DOCUMENT_COPY_FAILED;
import static uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent.DOCUMENT_NOT_FOUND;
import static uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent.DOCUMENT_RETRIEVAL_FAILED;
import static uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent.SQL_EXCEPTION;

@Component
@Slf4j
public class DocumentExtractor {

    private record DocStore(String fileName, byte[] bytes) {
    }

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

    public DocumentExtractor(@Qualifier("cms") DataSource dataSource, DocumentS3Client documentS3Client, DocumentsRepository documentsRepository) {
        this.dataSource = dataSource;
        this.documentS3Client = documentS3Client;
        this.documentsRepository = documentsRepository;
    }

    public List<CaseAttachment> copyDocumentsForCase(int caseId) {
        List<CaseAttachment> attachments = new ArrayList<>();
        List<BigDecimal> documentIds = queryDocumentIdsForCase(caseId);
        for (BigDecimal documentId : documentIds) {
            try {
                CaseAttachment attachment = getDocument(documentId.intValue(), caseId);
                if (attachment.getDocumentPath() != null) {
                    attachments.add(attachment);
                } else {
                    log.error("Document ID {} failed to extract for caset ID {}", documentId, caseId);
                }
            } catch (ApplicationExceptions.ExtractCaseException e) {
                log.error("Document extract failed for case ID :" + caseId + " " + e.getEvent() + " skipping case...");
            }
        }
        return attachments;
    }

    private CaseAttachment getDocument(int documentId, int caseId) {
        DocumentExtractRecord record = new DocumentExtractRecord();
        record.setDocumentId(documentId);
        record.setCaseId(caseId);
        String tempFileName = null;
        CaseAttachment caseAttachment = new CaseAttachment();
        DocStore doc = null;
        try {
            doc = queryForDocument(documentId);
        } catch (ApplicationExceptions.ExtractDocumentException e) {
            record.setDocumentExtracted(false);
            record.setFailureReason(e.getMessage());
            documentsRepository.save(record);
            throw new ApplicationExceptions.ExtractCaseException(
                    String.format("Failed to extract document for case: " + caseId), DOCUMENT_RETRIEVAL_FAILED);
        }

        try {
            tempFileName = documentS3Client.storeUntrustedDocument(doc.fileName(), doc.bytes(), documentId);
        } catch (ApplicationExceptions.ExtractDocumentException e) {
            record.setDocumentExtracted(false);
            record.setFailureReason(e.getMessage());
            documentsRepository.save(record);
            throw new ApplicationExceptions.ExtractCaseException(
                    String.format("Failed to copy document for case: " + caseId), DOCUMENT_COPY_FAILED);
        }

        record.setDocumentExtracted(true);
        record.setTempFileName(tempFileName);
        documentsRepository.save(record);
        caseAttachment.setDocumentPath(tempFileName);
        caseAttachment.setDisplayName(doc.fileName());
        return caseAttachment;
    }

    private List<BigDecimal> queryDocumentIdsForCase(int caseId) {
        Connection conn = null;
        PreparedStatement ps = null;
        List<BigDecimal> documentIds = new ArrayList<>();
        try {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement(DOCUMENTS_FOR_CASE);
            ps.setInt(1, caseId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                BigDecimal documentId = rs.getBigDecimal(1);
                documentIds.add(documentId);
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new ApplicationExceptions.ExtractDocumentException(
                    String.format("Failed to retrieve document IDs for case: " + caseId), SQL_EXCEPTION);
        } finally {
            try {
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
        }
        return documentIds;
    }

    private DocStore queryForDocument(int documentId) {
        Connection conn = null;
        PreparedStatement ps = null;
        DocStore docStore = null;
        try {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement(GET_DOCUMENT);
            ps.setInt(1, documentId);
            ResultSet res = ps.executeQuery();
            if (res.next()) {
                String fileName = res.getString(2);
                InputStream is = res.getBinaryStream(3);
                byte[] bytes = IOUtils.toByteArray(is);
                docStore = new DocStore(fileName, bytes);
            } else {
                log.error("Could not find document ID {}", documentId);
                throw new ApplicationExceptions.ExtractDocumentException(
                        String.format("Failed to retrieve document ID: ", documentId), DOCUMENT_NOT_FOUND);
            }
        } catch (SQLException e){
            log.error(e.getMessage());
            throw new ApplicationExceptions.ExtractDocumentException(
                    String.format("Failed to retrieve document ID: ", documentId), SQL_EXCEPTION);
        } catch (IOException e) {
            log.error("Failed to convert document ID: {} to bytes.", documentId);
            throw new ApplicationExceptions.ExtractDocumentException(
                    String.format("Failed to convert document ID: {} to bytes.", documentId), DOCUMENT_BYTE_CONVERSION_FAILED);
        } finally {
        try {
            if (ps != null)
                ps.close();
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
            log.error(e.getMessage());
            }
        }
        return docStore;
        }
}

