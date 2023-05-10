package uk.gov.digital.ho.hocs.cms.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.cms.casedata.CaseDataComplaintExtractor;
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
import uk.gov.digital.ho.hocs.cms.domain.model.ExtractRecord;
import uk.gov.digital.ho.hocs.cms.domain.repository.ExtractionStagesRepository;
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
    private final ExtractionStagesRepository extractionStagesRepository;
    private final CorrespondentExtractor correspondentExtractor;
    private final CaseDataComplaintExtractor caseDataComplaintExtractor;
    private final CompensationExtractor compensationExtractor;
    private final RiskAssessmentExtractor riskAssessmentExtractor;
    private final CaseLinkExtractor caseLinkExtractor;
    private final CategoriesExtractor categoriesExtractor;
    private final SubCategoriesExtractor subCategoriesExtractor;
    private final ResponseExtractor responseExtractor;
    private final CaseHistoryExtractor caseHistoryExtractor;
    private final ComplaintMessageBuilder complaintMessageBuilder;
    private final ExtractResult extractResult;
    private final MessageService messageService;
    private final ComplaintsMessageCaseData decsCaseData;
    private final DocumentCreator documentCreator;
    private final String migrationDocument;

    public ComplaintsService(DocumentExtractor documentExtractor,
                             ComplaintsExtractor complaintsExtractor,
                             ExtractionStagesRepository extractionStagesRepository,
                             CorrespondentExtractor correspondentExtractor,
                             CaseDataComplaintExtractor caseDataComplaintExtractor,
                             CompensationExtractor compensationExtractor,
                             RiskAssessmentExtractor riskAssessmentExtractor,
                             CaseLinkExtractor caseLinkExtractor,
                             CategoriesExtractor categoriesExtractor,
                             SubCategoriesExtractor subCategoriesExtractor,
                             ResponseExtractor responseExtractor,
                             CaseHistoryExtractor caseHistoryExtractor,
                             ComplaintsMessageCaseData decsCaseData,
                             ComplaintMessageBuilder complaintMessageBuilder,
                             ExtractResult extractResult,
                             MessageService messageService,
                             DocumentCreator documentCreator,
                             @Value("${migration.document}") String migrationDocument) {
        this.documentExtractor = documentExtractor;
        this.complaintsExtractor = complaintsExtractor;
        this.extractionStagesRepository = extractionStagesRepository;
        this.correspondentExtractor = correspondentExtractor;
        this.caseDataComplaintExtractor = caseDataComplaintExtractor;
        this.compensationExtractor = compensationExtractor;
        this.riskAssessmentExtractor = riskAssessmentExtractor;
        this.caseLinkExtractor = caseLinkExtractor;
        this.categoriesExtractor = categoriesExtractor;
        this.subCategoriesExtractor = subCategoriesExtractor;
        this.responseExtractor = responseExtractor;
        this.caseHistoryExtractor = caseHistoryExtractor;
        this.decsCaseData = decsCaseData;
        this.complaintMessageBuilder = complaintMessageBuilder;
        this.extractResult = extractResult;
        this.messageService = messageService;
        this.documentCreator = documentCreator;
        this.migrationDocument = migrationDocument;
    }

    public void migrateComplaints(String startDate, String endDate, ComplaintExtractionType extractionType) {
        List<BigDecimal> complaintIds = complaintsExtractor.getComplaintIdsByDateRange(startDate, endDate, extractionType);
        UUID extractionId = extractResult.saveExtractionId(complaintIds.size());
        for (BigDecimal complaintId : complaintIds) {
            log.info("Extract a single complaint started for complaint ID {}", complaintId);
            if (extractResult.recordExtractResult(extractComplaint(extractionId, complaintId), extractionId)) {
                log.info("Complaint extraction for complaint ID {}, extraction ID {} finished.", complaintId, extractionId);
            }
        }
        log.info("Complaints extraction for extraction ID {} between dates {} and {} finished.", extractionId, startDate, endDate);
    }

    public void migrateComplaints(List<String> complaintIds) {
        UUID extractionId = extractResult.saveExtractionId(complaintIds.size());
        for (String complaintId: complaintIds) {
            log.info("Extract a single complaint started for complaint ID {}", complaintId);
            if (extractResult.recordExtractResult(extractComplaint(extractionId, new BigDecimal(complaintId)), extractionId)) {
                log.info("Complaint extraction for complaint ID {}, extraction ID {} finished.", complaintId, extractionId);
            }
        }
    }

    public void migrateComplaint(String complaintId) {
        UUID extractionId = extractResult.saveExtractionId(1);
        if (extractResult.recordExtractResult(extractComplaint(extractionId, new BigDecimal(complaintId)), extractionId)) {
            log.info("Complaint extraction for complaint ID {}, extraction ID {} finished.", complaintId, extractionId);
        }
    }

    private boolean extractComplaint(UUID extractionId, BigDecimal complaintId) {
        CaseDetails caseDetails = new CaseDetails();
        // extract documents
        try {
            List<CaseAttachment> attachments = documentExtractor.copyDocumentsForCase(complaintId);
            caseDetails.setCaseAttachments(attachments);
            log.info("Extracted {} document(s) for complaint {}", attachments.size(), complaintId);
            ExtractRecord documentsStage = getComplaintExtractRecord(complaintId, extractionId, "Documents", true);
            extractionStagesRepository.save(documentsStage);
        } catch (ApplicationExceptions.ExtractCaseException e) {
            ExtractRecord documentsStage = getComplaintExtractRecord(complaintId, extractionId, "Documents", false);
            documentsStage.setError(e.getEvent().toString());
            documentsStage.setErrorMessage(e.getMessage());
            extractionStagesRepository.save(documentsStage);
            log.error("Failed documents for complaint ID {}", complaintId);
            return false;
        }

        // extract correspondents
        try {
            correspondentExtractor.getCorrespondentsForCase(complaintId);
            ExtractRecord correspondentStage = getComplaintExtractRecord(complaintId, extractionId, "Correspondents", true);
            extractionStagesRepository.save(correspondentStage);
        } catch (ApplicationExceptions.ExtractCorrespondentException e) {
            ExtractRecord correspondentStage = getComplaintExtractRecord(complaintId, extractionId, "Correspondents", false);
            correspondentStage.setError(e.getEvent().toString());
            correspondentStage.setErrorMessage(e.getMessage());
            extractionStagesRepository.save(correspondentStage);
            log.error("Failed extracting correspondents for complaint ID {}", complaintId);
            return false;
        }

        // extract case data
        try {
            caseDataComplaintExtractor.getCaseDataComplaint(complaintId);
            ExtractRecord caseDataStage = getComplaintExtractRecord(complaintId, extractionId, "Case data", true);
            extractionStagesRepository.save(caseDataStage);
        } catch (ApplicationExceptions.ExtractCaseDataException e) {
            ExtractRecord caseDataStage = getComplaintExtractRecord(complaintId, extractionId, "Case Data", false);
            caseDataStage.setError(e.getEvent().toString());
            caseDataStage.setErrorMessage(e.getMessage());
            extractionStagesRepository.save(caseDataStage);
            log.error("Failed extracting case data for complaint ID {}", complaintId);
            return false;
        }

        // extract compensation data
        try {
            compensationExtractor.getCompensationDetails(complaintId);
            ExtractRecord compensationStage = getComplaintExtractRecord(complaintId, extractionId, "Compensation", true);
            extractionStagesRepository.save(compensationStage);
        } catch (ApplicationExceptions.ExtractCompensationDataException e) {
            ExtractRecord compensationStage = getComplaintExtractRecord(complaintId, extractionId, "Compensation", false);
            compensationStage.setError(e.getEvent().toString());
            compensationStage.setErrorMessage(e.getMessage());
            extractionStagesRepository.save(compensationStage);
            log.error("Failed extracting compensation data for complaint ID {}", complaintId);
        }

        // extract risk assessment
        try {
            riskAssessmentExtractor.getRiskAssessment(complaintId);
            ExtractRecord riskAssessmentStage = getComplaintExtractRecord(complaintId, extractionId, "Risk assessment", true);
            extractionStagesRepository.save(riskAssessmentStage);
        } catch (ApplicationExceptions.ExtractRiskAssessmentException e) {
            ExtractRecord riskAssessmentStage = getComplaintExtractRecord(complaintId, extractionId, "Risk assessment", false);
            riskAssessmentStage.setError(e.getEvent().toString());
            riskAssessmentStage.setErrorMessage(e.getMessage());
            extractionStagesRepository.save(riskAssessmentStage);
            log.error("Failed extracting risk assessment for complaint ID {}", complaintId);
        }

        // extract case links
        try {
            caseLinkExtractor.getCaseLinks(complaintId);
            ExtractRecord caseLinksStage = getComplaintExtractRecord(complaintId, extractionId, "Case links", true);
            extractionStagesRepository.save(caseLinksStage);
        } catch (ApplicationExceptions.ExtractCaseLinksException e) {
            ExtractRecord caseLinksStage = getComplaintExtractRecord(complaintId, extractionId, "Case links", false);
            caseLinksStage.setError(e.getEvent().toString());
            caseLinksStage.setErrorMessage(e.getMessage());
            extractionStagesRepository.save(caseLinksStage);
            log.error("Failed extracting case links for complaint ID {}", complaintId);
        }

        // extract categories
        try {
            categoriesExtractor.getSelectedCategoryData(complaintId);
            subCategoriesExtractor.getSelectedSubCategoryData(complaintId);
            ExtractRecord categoriesStage = getComplaintExtractRecord(complaintId, extractionId, "Categories", true);
            extractionStagesRepository.save(categoriesStage);
        } catch (ApplicationExceptions.ExtractCategoriesException e) {
            ExtractRecord categoriesStage = getComplaintExtractRecord(complaintId, extractionId, "Categories", false);
            categoriesStage.setError(e.getEvent().toString());
            categoriesStage.setErrorMessage(e.getMessage());
            extractionStagesRepository.save(categoriesStage);
        }

        // extract response
        responseExtractor.getResponse(complaintId);

        // extract case history
        try {
            caseHistoryExtractor.getCaseHistory(complaintId);
            ExtractRecord caseHistoryStage = getComplaintExtractRecord(complaintId, extractionId, "Case history", true);
            extractionStagesRepository.save(caseHistoryStage);
        } catch (ApplicationExceptions.ExtractCaseHistoryException e) {
            ExtractRecord caseHistoryStage = getComplaintExtractRecord(complaintId, extractionId, "Case history", false);
            caseHistoryStage.setError(e.getEvent().toString());
            caseHistoryStage.setErrorMessage(e.getMessage());
            extractionStagesRepository.save(caseHistoryStage);
        }


        // create cms migration document
        if (migrationDocument.equalsIgnoreCase("enabled")) {
            try {
                CaseAttachment caseAttachment = documentCreator.createDocument(complaintId);
                caseDetails.addCaseAttachment(caseAttachment);
                ExtractRecord migrationDocumentStage = getComplaintExtractRecord(complaintId, extractionId, "Migration document", true);
                extractionStagesRepository.save(migrationDocumentStage);
            } catch (ApplicationExceptions.CreateMigrationDocumentException e) {
                ExtractRecord migrationDocumentStage = getComplaintExtractRecord(complaintId, extractionId, "Migration document", false);
                migrationDocumentStage.setError(e.getEvent().toString());
                migrationDocumentStage.setErrorMessage(e.getMessage());
                extractionStagesRepository.save(migrationDocumentStage);
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
        ExtractRecord correspondentStage = getComplaintExtractRecord(complaintId, extractionId, "Migration message", true);
        extractionStagesRepository.save(correspondentStage);
        } catch (ApplicationExceptions.SendMigrationMessageException e) {
            ExtractRecord correspondentStage = getComplaintExtractRecord(complaintId, extractionId, "Migration message", false);
            correspondentStage.setError(e.getEvent().toString());
            correspondentStage.setErrorMessage(e.getMessage());
            extractionStagesRepository.save(correspondentStage);
            log.error("Failed sending migration message for complaint ID {}", complaintId + " skipping case...");
            return false;
        }
        return true;
    }

    private ExtractRecord getComplaintExtractRecord(BigDecimal complaintId, UUID extractionId, String stage, boolean extracted) {
        ExtractRecord cer = new ExtractRecord();
        cer.setExtractionId(extractionId);
        cer.setCaseId(complaintId);
        cer.setExtracted(extracted);
        cer.setStage(stage);
        return cer;
    }
}
