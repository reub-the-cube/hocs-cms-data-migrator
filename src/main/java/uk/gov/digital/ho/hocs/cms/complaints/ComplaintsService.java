package uk.gov.digital.ho.hocs.cms.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.cms.casedata.CaseDataExtractor;
import uk.gov.digital.ho.hocs.cms.caselinks.CaseLinkExtractor;
import uk.gov.digital.ho.hocs.cms.categories.CategoriesExtractor;
import uk.gov.digital.ho.hocs.cms.categories.SubCategoriesExtractor;
import uk.gov.digital.ho.hocs.cms.client.MessageService;
import uk.gov.digital.ho.hocs.cms.compensation.CompensationExtractor;
import uk.gov.digital.ho.hocs.cms.correspondents.CorrespondentExtractor;
import uk.gov.digital.ho.hocs.cms.documents.DocumentCreator;
import uk.gov.digital.ho.hocs.cms.documents.DocumentExtractor;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseAttachment;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseDetails;
import uk.gov.digital.ho.hocs.cms.domain.model.ComplaintExtractRecord;
import uk.gov.digital.ho.hocs.cms.domain.model.Progress;
import uk.gov.digital.ho.hocs.cms.domain.repository.ComplaintsRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.ProgressRepository;
import uk.gov.digital.ho.hocs.cms.history.CaseHistoryExtractor;
import uk.gov.digital.ho.hocs.cms.response.ResponseExtractor;
import uk.gov.digital.ho.hocs.cms.risk.RiskAssessmentExtractor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ComplaintsService {

    private final DocumentExtractor documentExtractor;
    private final ComplaintsExtractor complaintsExtractor;
    private final ComplaintsRepository complaintsRepository;
    private final ProgressRepository progressRepository;
    private final CorrespondentExtractor correspondentExtractor;
    private final CaseDataExtractor caseDataExtractor;
    private final CompensationExtractor compensationExtractor;
    private final RiskAssessmentExtractor riskAssessmentExtractor;
    private final CaseLinkExtractor caseLinkExtractor;
    private final CategoriesExtractor categoriesExtractor;
    private final SubCategoriesExtractor subCategoriesExtractor;
    private final ResponseExtractor responseExtractor;
    private final CaseHistoryExtractor caseHistoryExtractor;
    private final ComplaintMessageBuilder complaintMessageBuilder;
    private final MessageService messageService;
    private final ComplaintsMessageCaseData decsCaseData;
    private final DocumentCreator documentCreator;
    private final String migrationDocument;

    public ComplaintsService(DocumentExtractor documentExtractor,
                             ComplaintsExtractor complaintsExtractor,
                             ComplaintsRepository complaintsRepository,
                             ProgressRepository progressRepository,
                             CorrespondentExtractor correspondentExtractor,
                             CaseDataExtractor caseDataExtractor,
                             CompensationExtractor compensationExtractor,
                             RiskAssessmentExtractor riskAssessmentExtractor,
                             CaseLinkExtractor caseLinkExtractor,
                             CategoriesExtractor categoriesExtractor,
                             SubCategoriesExtractor subCategoriesExtractor,
                             ResponseExtractor responseExtractor,
                             CaseHistoryExtractor caseHistoryExtractor,
                             ComplaintsMessageCaseData decsCaseData,
                             ComplaintMessageBuilder complaintMessageBuilder,
                             MessageService messageService,
                             DocumentCreator documentCreator,
                             @Value("${migration.document}") String migrationDocument) {
        this.documentExtractor = documentExtractor;
        this.complaintsExtractor = complaintsExtractor;
        this.complaintsRepository = complaintsRepository;
        this.progressRepository = progressRepository;
        this.correspondentExtractor = correspondentExtractor;
        this.caseDataExtractor = caseDataExtractor;
        this.compensationExtractor = compensationExtractor;
        this.riskAssessmentExtractor = riskAssessmentExtractor;
        this.caseLinkExtractor = caseLinkExtractor;
        this.categoriesExtractor = categoriesExtractor;
        this.subCategoriesExtractor = subCategoriesExtractor;
        this.responseExtractor = responseExtractor;
        this.caseHistoryExtractor = caseHistoryExtractor;
        this.decsCaseData = decsCaseData;
        this.complaintMessageBuilder = complaintMessageBuilder;
        this.messageService = messageService;
        this.documentCreator = documentCreator;
        this.migrationDocument = migrationDocument;
    }

    @Transactional
    public void migrateComplaints(String startDate, String endDate) {
        List<BigDecimal> complaintIds = complaintsExtractor.getComplaintIdsByDateRange(startDate, endDate);
        UUID extractionId = UUID.randomUUID();
        log.info("Extraction ID {}", extractionId);
        Progress progress = new Progress();
        progress.setExtractionId(extractionId);
        progressRepository.save(progress);
        for (BigDecimal complaintId : complaintIds) {
            recordExtractResult(extractComplaint(extractionId, complaintId), extractionId);
        }
        log.info("Complaints extraction for extraction ID {} between dates {} and {} finished.", extractionId, startDate, endDate);
    }

    @Transactional
    public void migrateComplaint(String complaintId) {
        UUID extractionId = UUID.randomUUID();
        recordExtractResult(extractComplaint(extractionId, new BigDecimal(complaintId)), extractionId);
        log.info("Complaint extraction for complaint ID {}, extraction ID {} finished", complaintId, extractionId);
    }

    private void recordExtractResult(boolean result, UUID extractionId) {
        if (result) progressRepository.incrementSuccess(1, extractionId);
        else progressRepository.incrementFailure(1, extractionId);
    }

    private boolean extractComplaint(UUID extractionId, BigDecimal complaintId) {
        CaseDetails caseDetails = new CaseDetails();
        // extract documents
        try {
            List<CaseAttachment> attachments = documentExtractor.copyDocumentsForCase(complaintId);
            caseDetails.setCaseAttachments(attachments);
            log.info("Extracted {} document(s) for complaint {}", attachments.size(), complaintId);
            ComplaintExtractRecord documentsStage = getComplaintExtractRecord(complaintId, extractionId, "Documents", true);
            complaintsRepository.save(documentsStage);
        } catch (ApplicationExceptions.ExtractCaseException e) {
            ComplaintExtractRecord documentsStage = getComplaintExtractRecord(complaintId, extractionId, "Documents", false);
            documentsStage.setError(e.getEvent().toString());
            documentsStage.setErrorMessage(e.getMessage());
            complaintsRepository.save(documentsStage);
            log.error("Failed documents for complaint ID {}", complaintId);
            return false;
        }

        // extract correspondents
        try {
            correspondentExtractor.getCorrespondentsForCase(complaintId);
            ComplaintExtractRecord correspondentStage = getComplaintExtractRecord(complaintId, extractionId, "Correspondents", true);
            complaintsRepository.save(correspondentStage);
        } catch (ApplicationExceptions.ExtractCorrespondentException e) {
            ComplaintExtractRecord correspondentStage = getComplaintExtractRecord(complaintId, extractionId, "Correspondents", false);
            correspondentStage.setError(e.getEvent().toString());
            correspondentStage.setErrorMessage(e.getMessage());
            complaintsRepository.save(correspondentStage);
            log.error("Failed extracting correspondents for complaint ID {}", complaintId);
            return false;
        }

        // extract case data
        try {
            caseDataExtractor.getCaseData(complaintId);
            ComplaintExtractRecord caseDataStage = getComplaintExtractRecord(complaintId, extractionId, "Case data", true);
            complaintsRepository.save(caseDataStage);
        } catch (ApplicationExceptions.ExtractCaseDataException e) {
            ComplaintExtractRecord caseDataStage = getComplaintExtractRecord(complaintId, extractionId, "Case Data", false);
            caseDataStage.setError(e.getEvent().toString());
            caseDataStage.setErrorMessage(e.getMessage());
            complaintsRepository.save(caseDataStage);
            log.error("Failed extracting case data for complaint ID {}", complaintId);
            return false;
        }

        // extract compensation data
        try {
            compensationExtractor.getCompensationDetails(complaintId);
            ComplaintExtractRecord compensationStage = getComplaintExtractRecord(complaintId, extractionId, "Compensation", true);
            complaintsRepository.save(compensationStage);
        } catch (ApplicationExceptions.ExtractCompensationDataException e) {
            ComplaintExtractRecord compensationStage = getComplaintExtractRecord(complaintId, extractionId, "Compensation", false);
            compensationStage.setError(e.getEvent().toString());
            compensationStage.setErrorMessage(e.getMessage());
            complaintsRepository.save(compensationStage);
            log.error("Failed extracting compensation data for complaint ID {}", complaintId);
        }

        // extract risk assessment
        try {
            riskAssessmentExtractor.getRiskAssessment(complaintId);
            ComplaintExtractRecord riskAssessmentStage = getComplaintExtractRecord(complaintId, extractionId, "Risk assessment", true);
            complaintsRepository.save(riskAssessmentStage);
        } catch (ApplicationExceptions.ExtractRiskAssessmentException e) {
            ComplaintExtractRecord riskAssessmentStage = getComplaintExtractRecord(complaintId, extractionId, "Risk assessment", false);
            riskAssessmentStage.setError(e.getEvent().toString());
            riskAssessmentStage.setErrorMessage(e.getMessage());
            complaintsRepository.save(riskAssessmentStage);
            log.error("Failed extracting risk assessment for complaint ID {}", complaintId);
        }

        // extract case links
        try {
            caseLinkExtractor.getCaseLinks(complaintId);
            ComplaintExtractRecord caseLinksStage = getComplaintExtractRecord(complaintId, extractionId, "Case links", true);
            complaintsRepository.save(caseLinksStage);
        } catch (ApplicationExceptions.ExtractCaseLinksException e) {
            ComplaintExtractRecord caseLinksStage = getComplaintExtractRecord(complaintId, extractionId, "Case links", false);
            caseLinksStage.setError(e.getEvent().toString());
            caseLinksStage.setErrorMessage(e.getMessage());
            complaintsRepository.save(caseLinksStage);
            log.error("Failed extracting case links for complaint ID {}", complaintId);
        }

        // extract categories
        categoriesExtractor.getSelectedCategoryData(complaintId);
        subCategoriesExtractor.getSelectedSubCategoryData(complaintId);

        // extract response
        responseExtractor.getResponse(complaintId);

        // extract case history
        caseHistoryExtractor.getCaseHistory(complaintId);


        // create cms migration document
        if (migrationDocument.equalsIgnoreCase("enabled")) {
            try {
                CaseAttachment caseAttachment = documentCreator.createDocument(complaintId);
                caseDetails.addCaseAttachment(caseAttachment);
                ComplaintExtractRecord migrationDocumentStage = getComplaintExtractRecord(complaintId, extractionId, "Migration document", true);
                complaintsRepository.save(migrationDocumentStage);
            } catch (ApplicationExceptions.CreateMigrationDocumentException e) {
                ComplaintExtractRecord migrationDocumentStage = getComplaintExtractRecord(complaintId, extractionId, "Migration document", false);
                migrationDocumentStage.setError(e.getEvent().toString());
                migrationDocumentStage.setErrorMessage(e.getMessage());
                complaintsRepository.save(migrationDocumentStage);
                log.error("Failed creating Migration PDF for complaint ID {}", complaintId);
            }
        }

        // populate message
        try {
        CaseDetails message = complaintMessageBuilder.buildMessage(complaintId);
        message.addCaseDataItems(decsCaseData.extractCaseData(complaintId));
        message.setCaseAttachments(caseDetails.getCaseAttachments());
        // send migration message
        message.setSourceCaseId(complaintId.toString());
        messageService.sendMigrationMessage(message);
        ComplaintExtractRecord correspondentStage = getComplaintExtractRecord(complaintId, extractionId, "Migration message", true);
        complaintsRepository.save(correspondentStage);
        } catch (ApplicationExceptions.SendMigrationMessageException e) {
            ComplaintExtractRecord correspondentStage = getComplaintExtractRecord(complaintId, extractionId, "Migration message", false);
            correspondentStage.setError(e.getEvent().toString());
            correspondentStage.setErrorMessage(e.getMessage());
            complaintsRepository.save(correspondentStage);
            log.error("Failed sending migration message for complaint ID {}", complaintId + " skipping case...");
            return false;
        }
        return true;
    }

    private ComplaintExtractRecord getComplaintExtractRecord(BigDecimal complaintId, UUID extractionId, String stage, boolean extracted) {
        ComplaintExtractRecord cer = new ComplaintExtractRecord();
        cer.setExtractionId(extractionId);
        cer.setCaseId(complaintId);
        cer.setComplaintExtracted(extracted);
        cer.setStage(stage);
        return cer;
    }
}
