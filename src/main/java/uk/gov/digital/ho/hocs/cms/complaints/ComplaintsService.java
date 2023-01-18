package uk.gov.digital.ho.hocs.cms.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.cms.casedata.CaseDataExtractor;
import uk.gov.digital.ho.hocs.cms.client.MessageService;
import uk.gov.digital.ho.hocs.cms.compensation.CompensationExtractor;
import uk.gov.digital.ho.hocs.cms.correspondents.CorrespondentExtractor;
import uk.gov.digital.ho.hocs.cms.documents.DocumentExtractor;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseDetails;
import uk.gov.digital.ho.hocs.cms.domain.model.ComplaintExtractRecord;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseAttachment;
import uk.gov.digital.ho.hocs.cms.domain.repository.ComplaintsRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class ComplaintsService {

    private final DocumentExtractor documentExtractor;
    private final ComplaintsExtractor complaintsExtractor;
    private final ComplaintsRepository complaintsRepository;
    private final CorrespondentExtractor correspondentExtractor;
    private final CaseDataExtractor caseDataExtractor;
    private final MessageService messageService;
    private final CompensationExtractor compensationExtractor;

    public ComplaintsService(DocumentExtractor documentExtractor,
                             ComplaintsExtractor complaintsExtractor,
                             ComplaintsRepository complaintsRepository,
                             CorrespondentExtractor correspondentExtractor,
                             CaseDataExtractor caseDataExtractor,
                             CompensationExtractor compensationExtractor,
                             MessageService messageService) {
        this.documentExtractor = documentExtractor;
        this.complaintsExtractor = complaintsExtractor;
        this.complaintsRepository = complaintsRepository;
        this.correspondentExtractor = correspondentExtractor;
        this.caseDataExtractor = caseDataExtractor;
        this.compensationExtractor = compensationExtractor;
        this.messageService = messageService;
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
        CaseDetails caseDetails = new CaseDetails();
        // extract documents
        try {
            List<CaseAttachment> attachments = documentExtractor.copyDocumentsForCase(complaintId);
            caseDetails.setCaseAttachments(attachments);
            log.info("Extracted {} document(s) for complaint {}", attachments.size(), complaintId);
            ComplaintExtractRecord documentsStage = getComplaintExtractRecord(complaintId, "Documents", true);
            complaintsRepository.save(documentsStage);
        } catch (ApplicationExceptions.ExtractDocumentException e) {
            ComplaintExtractRecord documentsStage = getComplaintExtractRecord(complaintId, "Documents", false);
            documentsStage.setError(e.getEvent().toString());
            documentsStage.setErrorMessage(e.getMessage());
            complaintsRepository.save(documentsStage);
            log.error("Failed documents for complaint ID {}", complaintId + " skipping case...");
            return;
        }

        // extract correspondents
        try {
            caseDetails.setSourceCaseId(complaintId.toString());
            correspondentExtractor.getCorrespondentsForCase(complaintId, caseDetails);
            ComplaintExtractRecord correspondentStage = getComplaintExtractRecord(complaintId, "Correspondents", true);
            complaintsRepository.save(correspondentStage);
        } catch (ApplicationExceptions.ExtractCorrespondentException e) {
            ComplaintExtractRecord correspondentStage = getComplaintExtractRecord(complaintId, "Correspondents", false);
            correspondentStage.setError(e.getEvent().toString());
            correspondentStage.setErrorMessage(e.getMessage());
            complaintsRepository.save(correspondentStage);
            log.error("Failed extracting correspondents for complaint ID {}", complaintId + " skipping case...");
            return;
        }

        // extract case data
        try {
            caseDataExtractor.getCaseData(complaintId, caseDetails);
            ComplaintExtractRecord caseDataStage = getComplaintExtractRecord(complaintId, "Case data", true);
            complaintsRepository.save(caseDataStage);
        } catch (ApplicationExceptions.ExtractCaseDataException e) {
            ComplaintExtractRecord correspondentStage = getComplaintExtractRecord(complaintId, "Case Data", false);
            correspondentStage.setError(e.getEvent().toString());
            correspondentStage.setErrorMessage(e.getMessage());
            complaintsRepository.save(correspondentStage);
            log.error("Failed extracting case data for complaint ID {}", complaintId);
        }

        // extract compensation data
        try {
            compensationExtractor.getCompensationDetails(complaintId);
            ComplaintExtractRecord caseDataStage = getComplaintExtractRecord(complaintId, "Compensation", true);
            complaintsRepository.save(caseDataStage);
        } catch (ApplicationExceptions.ExtractCompensationDataException e) {
            ComplaintExtractRecord correspondentStage = getComplaintExtractRecord(complaintId, "Compensation Data", false);
            correspondentStage.setError(e.getEvent().toString());
            correspondentStage.setErrorMessage(e.getMessage());
            complaintsRepository.save(correspondentStage);
            log.error("Failed extracting compensation data for complaint ID {}", complaintId);
        }

        // TODO: Extract additional complaint data
        // TODO: Check case record and build migration message

        // send migration message
        try {
            messageService.sendMigrationMessage(caseDetails);
        } catch (ApplicationExceptions.SendMigrationMessageException e) {
            ComplaintExtractRecord correspondentStage = getComplaintExtractRecord(complaintId, "Migration message", false);
            correspondentStage.setError(e.getEvent().toString());
            correspondentStage.setErrorMessage(e.getMessage());
            complaintsRepository.save(correspondentStage);
            log.error("Failed sending migration message for complaint ID {}", complaintId + " skipping case...");
        }
    }

    private ComplaintExtractRecord getComplaintExtractRecord(BigDecimal complaintId, String stage, boolean extracted) {
        ComplaintExtractRecord cer = new ComplaintExtractRecord();
        cer.setCaseId(complaintId);
        cer.setComplaintExtracted(extracted);
        cer.setStage(stage);
        return cer;
    }
}
