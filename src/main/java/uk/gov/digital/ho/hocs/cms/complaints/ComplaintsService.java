package uk.gov.digital.ho.hocs.cms.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.cms.documents.DocumentExtrator;
import uk.gov.digital.ho.hocs.cms.domain.ComplaintExtractRecord;
import uk.gov.digital.ho.hocs.cms.domain.repository.ComplaintsRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.DocumentsRepository;
import uk.gov.digital.ho.hocs.cms.message.CaseAttachment;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

@Service
@Slf4j
public class ComplaintsService {

    private final DocumentExtrator documentExtrator;
    private final ComplaintExtractor complaintsExtractor;
    private final ComplaintsRepository complaintsRepository;
    private final DocumentsRepository documentsRepository;

    private final String startDate;
    private final String endDate;

    public ComplaintsService(@Value("${complaint.start.date}") String startDate,
                             @Value("${complaint.end.date}") String endDate,
                             DocumentExtrator documentExtrator,
                             ComplaintExtractor complaintsExtractor,
                             ComplaintsRepository complaintsRepository,
                             DocumentsRepository documentsRepository) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.documentExtrator = documentExtrator;
        this.complaintsExtractor = complaintsExtractor;
        this.complaintsRepository = complaintsRepository;
        this.documentsRepository = documentsRepository;
    }
    public void migrate() throws SQLException {
        List<BigDecimal> complaints = complaintsExtractor.getComplaintIdsByDateRange(startDate, endDate);
        log.info("{} complaints found for dates {} to {}.", complaints.size(), startDate, endDate);
        for (BigDecimal complaint : complaints) {
            ComplaintExtractRecord cer = new ComplaintExtractRecord();
            List<CaseAttachment> attachments = documentExtrator.copyDocumentsForCase(complaint.intValue());
            log.info("Extracted {} document(s) for complaint {}", attachments.size(), complaint.intValue());
            if (documentsRepository.findFailedDocumentsForCase(complaint.intValue()) == 0) {
                cer.setCaseId(complaint.intValue());
                cer.setComplaintExtracted(true);
            } else {
                cer.setCaseId(complaint.intValue());
                cer.setComplaintExtracted(false);
                log.error("Failed documents for complaint ID {}", complaint.intValue());
        }
            cer.setStage("Documents");
            complaintsRepository.save(cer);
        }
        // TODO: Extract additional complaint data
        // TODO: Check case record and build migration message
        log.info("Complaints extraction between dates {} and {} finished.", startDate, endDate);
    }
}
