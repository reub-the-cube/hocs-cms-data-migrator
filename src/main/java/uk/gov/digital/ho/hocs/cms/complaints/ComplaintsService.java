package uk.gov.digital.ho.hocs.cms.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.cms.correspondents.CorrespondentExtractor;
import uk.gov.digital.ho.hocs.cms.documents.DocumentExtractor;
import uk.gov.digital.ho.hocs.cms.domain.ComplaintExtractRecord;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseAttachment;
import uk.gov.digital.ho.hocs.cms.domain.repository.ComplaintsRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.DocumentsRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class ComplaintsService {

    private final DocumentExtractor documentExtractor;
    private final DocumentsRepository documentsRepository;
    private final ComplaintsExtractor complaintsExtractor;
    private final ComplaintsRepository complaintsRepository;
    private final CorrespondentExtractor correspondentExtractor;

    public ComplaintsService(DocumentExtractor documentExtractor,
                             DocumentsRepository documentsRepository,
                             ComplaintsExtractor complaintsExtractor,
                             ComplaintsRepository complaintsRepository,
                             CorrespondentExtractor correspondentExtractor) {
        this.documentExtractor = documentExtractor;
        this.documentsRepository = documentsRepository;
        this.complaintsExtractor = complaintsExtractor;
        this.complaintsRepository = complaintsRepository;
        this.correspondentExtractor = correspondentExtractor;
    }
    public void migrateComplaints(String startDate, String endDate) {
        List<BigDecimal> complaints = complaintsExtractor.getComplaintIdsByDateRange(startDate, endDate);
        log.info("{} complaints found for dates {} to {}.", complaints.size(), startDate, endDate);
        for (BigDecimal complaint : complaints) {
            extractComplaint(complaint);
        }
        log.info("Complaints extraction between dates {} and {} finished.", startDate, endDate);
    }

    public void migrateComplaint(String complaintId) {
        extractComplaint(new BigDecimal(complaintId));
        log.info("Complaint extraction for complaint ID {} finished", complaintId);
    }

    private void extractComplaint(BigDecimal complaintId) {
        ComplaintExtractRecord cer = new ComplaintExtractRecord();
        try {
            List<CaseAttachment> attachments = documentExtractor.copyDocumentsForCase(complaintId);
            log.info("Extracted {} document(s) for complaint {}", attachments.size(), complaintId);
        } catch (ApplicationExceptions.ExtractCaseException e) {
            cer.setCaseId(complaintId);
            cer.setComplaintExtracted(false);
            cer.setStage("Documents");
            complaintsRepository.save(cer);
            log.error("Failed documents for complaint ID {}", complaintId + " skipping case...");
        }
        cer.setCaseId(complaintId);
        cer.setComplaintExtracted(true);
        cer.setStage("Documents");
        complaintsRepository.save(cer);

        correspondentExtractor.getCorrespondentsForCase(complaintId);

        // TODO: Extract additional complaint data
        // TODO: Check case record and build migration message
    }


}
