package uk.gov.digital.ho.hocs.cms.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.cms.documents.DocumentExtractor;
import uk.gov.digital.ho.hocs.cms.domain.ComplaintExtractRecord;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseAttachment;
import uk.gov.digital.ho.hocs.cms.domain.repository.ComplaintsRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.DocumentsRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class ComplaintsService {

    private final DocumentExtractor documentExtrator;
    private final DocumentsRepository documentsRepository;
    private final ComplaintsExtractor complaintsExtractor;
    private final ComplaintsRepository complaintsRepository;

    public ComplaintsService(DocumentExtractor documentExtrator,
                             DocumentsRepository documentsRepository,
                             ComplaintsExtractor complaintsExtractor,
                             ComplaintsRepository complaintsRepository) {
        this.documentExtrator = documentExtrator;
        this.documentsRepository = documentsRepository;
        this.complaintsExtractor = complaintsExtractor;
        this.complaintsRepository = complaintsRepository;
    }
    public void migrateComplaints(String startDate, String endDate) {
        List<BigDecimal> complaints = complaintsExtractor.getComplaintIdsByDateRange(startDate, endDate);
        log.info("{} complaints found for dates {} to {}.", complaints.size(), startDate, endDate);
        for (BigDecimal complaint : complaints) {
            extractComplaint(complaint.intValue());
        }
        log.info("Complaints extraction between dates {} and {} finished.", startDate, endDate);
    }

    public void migrateComplaint(String complaintId) {
        extractComplaint(Integer.parseInt(complaintId));
        log.info("Complaint extraction for complaint ID {} finished", complaintId);
    }

    private void extractComplaint(int complaintId) {
        ComplaintExtractRecord cer = new ComplaintExtractRecord();
        List<CaseAttachment> attachments = documentExtrator.copyDocumentsForCase(complaintId);
        log.info("Extracted {} document(s) for complaint {}", attachments.size(), complaintId);
        if (documentsRepository.findFailedDocumentsForCase(complaintId) == 0) {
            cer.setCaseId(complaintId);
            cer.setComplaintExtracted(true);
        } else {
            cer.setCaseId(complaintId);
            cer.setComplaintExtracted(false);
            log.error("Failed documents for complaint ID {}", complaintId);
    }
        cer.setStage("Documents");
        complaintsRepository.save(cer);

        // TODO: Extract additional complaint data
        // TODO: Check case record and build migration message
    }


}
