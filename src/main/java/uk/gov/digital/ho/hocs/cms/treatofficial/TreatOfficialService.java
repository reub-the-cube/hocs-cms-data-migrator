package uk.gov.digital.ho.hocs.cms.treatofficial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import uk.gov.digital.ho.hocs.cms.caselinks.CaseLinkExtractor;
import uk.gov.digital.ho.hocs.cms.client.MessageService;
import uk.gov.digital.ho.hocs.cms.complaints.ExtractResult;
import uk.gov.digital.ho.hocs.cms.documents.DocumentExtractor;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseAttachment;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseDetails;
import uk.gov.digital.ho.hocs.cms.domain.model.ExtractRecord;
import uk.gov.digital.ho.hocs.cms.domain.repository.ExtractionStagesRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.ProgressRepository;
import uk.gov.digital.ho.hocs.cms.history.CaseHistoryExtractor;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TreatOfficialService {

    private final TreatOfficialExtractor treatOfficialExtractor;
    private final CaseDataTreatOfficialExtractor caseDataTreatOfficialExtractor;
    private final ProgressRepository progressRepository;
    private final TreatOfficialCorrespondentExtractor treatOfficialCorrespondentExtractor;
    private final ExtractionStagesRepository extractionStagesRepository;
    private final TreatOfficialMessageBuilder treatOfficialMessageBuilder;
    private final TreatOfficialMessageCaseData treatOfficialMessageCaseData;
    private final MessageService messageService;
    private final ExtractResult extractResult;
    private final CaseLinkExtractor caseLinkExtractor;
    private final CaseHistoryExtractor caseHistoryExtractor;
    private final DocumentExtractor documentExtractor;
    private final TreatOfficialDocumentCreator treatOfficialDocumentCreator;
    private final String migrationDocument;


    public TreatOfficialService(TreatOfficialExtractor treatOfficialExtractor,
                                TreatOfficialCorrespondentExtractor treatOfficialCorrespondentExtractor,
                                CaseDataTreatOfficialExtractor caseDataTreatOfficialExtractor,
                                ExtractionStagesRepository extractionStagesRepository,
                                TreatOfficialMessageBuilder treatOfficialMessageBuilder,
                                ExtractResult extractResult,
                                ProgressRepository progressRepository,
                                TreatOfficialMessageCaseData treatOfficialMessageCaseData,
                                MessageService messageService,
                                CaseLinkExtractor caseLinkExtractor,
                                CaseHistoryExtractor caseHistoryExtractor,
                                DocumentExtractor documentExtractor,
                                TreatOfficialDocumentCreator treatOfficialDocumentCreator,
                                @Value("${migration.document}") String migrationDocument) {
        this.progressRepository = progressRepository;
        this.treatOfficialExtractor = treatOfficialExtractor;
        this.treatOfficialCorrespondentExtractor = treatOfficialCorrespondentExtractor;
        this.caseDataTreatOfficialExtractor = caseDataTreatOfficialExtractor;
        this.extractResult = extractResult;
        this.extractionStagesRepository = extractionStagesRepository;
        this.treatOfficialMessageBuilder = treatOfficialMessageBuilder;
        this.treatOfficialMessageCaseData = treatOfficialMessageCaseData;
        this.messageService = messageService;
        this.caseLinkExtractor = caseLinkExtractor;
        this.caseHistoryExtractor = caseHistoryExtractor;
        this.documentExtractor = documentExtractor;
        this.treatOfficialDocumentCreator = treatOfficialDocumentCreator;
        this.migrationDocument = migrationDocument;
    }

    public void migrateTreatOfficials(String startDate, String endDate) {
        List<BigDecimal> treatOfficialIds = treatOfficialExtractor.getCaseIdsByDateRange(startDate, endDate);
        UUID extractionId = extractResult.saveExtractionId(treatOfficialIds.size());
        for (BigDecimal treatOfficialId : treatOfficialIds) {
            log.info("Extract a single Treat Official case started for case ID {}", treatOfficialId);
            if (extractResult.recordExtractResult(extractTreatOfficial(extractionId, treatOfficialId), extractionId)) {
                log.info("Treat Official case extraction for case ID {}, extraction ID {} finished.", treatOfficialId, extractionId);
            }
        }
        log.info("Treat Official extraction for extraction ID {} between dates {} and {} finished.", extractionId, startDate, endDate);
    }

    public void migrateTreatOfficials(List<String> caseIds) {
        UUID extractionId = extractResult.saveExtractionId(caseIds.size());
        for (String caseId: caseIds) {
            log.info("Extract a single Treat Official case started for case ID {}", caseId);
            if (extractResult.recordExtractResult(extractTreatOfficial(extractionId, new BigDecimal(caseId)), extractionId)) {
                log.info("Treat Official case extraction for case ID {}, extraction ID {} finished.", caseId, extractionId);
            }
        }
    }

    private boolean extractTreatOfficial(UUID extractionId, BigDecimal caseId) {
        CaseDetails caseDetails = new CaseDetails();

        //extract case-attachments
        try {
            List<CaseAttachment> attachments = documentExtractor.copyDocumentsForCase(caseId);
            caseDetails.setCaseAttachments(attachments);
            log.info("Extracted {} document(s) for Treat Official case {}", attachments.size(), caseId);
            ExtractRecord documentsStage = getTreatOfficialExtractRecord(caseId, extractionId, "Documents", true);
            extractionStagesRepository.save(documentsStage);
        } catch (ApplicationExceptions.ExtractCaseException e) {
            ExtractRecord documentsStage = getTreatOfficialExtractRecord(caseId, extractionId, "Documents", false);
            documentsStage.setError(e.getEvent().toString());
            documentsStage.setErrorMessage(e.getMessage());
            extractionStagesRepository.save(documentsStage);
            log.error("Failed documents for Treat Official case ID {}", caseId);
            return false;
        }

        //extract case-data
        try {
            caseDataTreatOfficialExtractor.getCaseDataTreatOfficial(caseId);
            ExtractRecord caseDataStage = getTreatOfficialExtractRecord(caseId, extractionId, "Case data Treat Official", true);
            extractionStagesRepository.save(caseDataStage);
        } catch (ApplicationExceptions.ExtractCaseDataException e) {
            ExtractRecord caseDataStage = getTreatOfficialExtractRecord(caseId, extractionId, "Case Data Treat Official", false);
            caseDataStage.setError(e.getEvent().toString());
            caseDataStage.setErrorMessage(e.getMessage());
            extractionStagesRepository.save(caseDataStage);
            log.error("Failed extracting Treat Official case data for case ID {}", caseId);
            return false;
        }

        //extract correspondent
        try {
            treatOfficialCorrespondentExtractor.getCorrespondentsForCase(caseId);
            ExtractRecord correspondentStage = getTreatOfficialExtractRecord(caseId, extractionId, "Correspondents", true);
            extractionStagesRepository.save(correspondentStage);
        } catch (ApplicationExceptions.ExtractCorrespondentException e) {
            ExtractRecord correspondentStage = getTreatOfficialExtractRecord(caseId, extractionId, "Correspondents", false);
            correspondentStage.setError(e.getEvent().toString());
            correspondentStage.setErrorMessage(e.getMessage());
            extractionStagesRepository.save(correspondentStage);
            log.error("Failed extracting correspondents for Treat Official case ID {}", caseId);
            return false;
        }

        //extract case-links
        try {
            caseLinkExtractor.getCaseLinks(caseId);
            ExtractRecord caseLinksStage = getTreatOfficialExtractRecord(caseId, extractionId, "Case links", true);
            extractionStagesRepository.save(caseLinksStage);
        } catch (ApplicationExceptions.ExtractCaseLinksException e) {
            ExtractRecord caseLinksStage = getTreatOfficialExtractRecord(caseId, extractionId, "Case links", false);
            caseLinksStage.setError(e.getEvent().toString());
            caseLinksStage.setErrorMessage(e.getMessage());
            extractionStagesRepository.save(caseLinksStage);
            log.error("Failed extracting case links for Treat Official case ID {}", caseId);
        }

        // extract case history
        try {
            caseHistoryExtractor.getCaseHistory(caseId);
            ExtractRecord caseHistoryStage = getTreatOfficialExtractRecord(caseId, extractionId, "Case history", true);
            extractionStagesRepository.save(caseHistoryStage);
        } catch (ApplicationExceptions.ExtractCaseHistoryException e) {
            ExtractRecord caseHistoryStage = getTreatOfficialExtractRecord(caseId, extractionId, "Case history", false);
            caseHistoryStage.setError(e.getEvent().toString());
            caseHistoryStage.setErrorMessage(e.getMessage());
            extractionStagesRepository.save(caseHistoryStage);
        }

        //create migration document
        if (migrationDocument.equalsIgnoreCase("enabled")) {
            try {
                CaseAttachment caseAttachment = treatOfficialDocumentCreator.createDocument(caseId);
                //caseDetails.addCaseAttachment(caseAttachment);
                ExtractRecord migrationDocumentStage = getTreatOfficialExtractRecord(caseId, extractionId, "Migration document", true);
                extractionStagesRepository.save(migrationDocumentStage);
            } catch (ApplicationExceptions.CreateMigrationDocumentException e) {
                ExtractRecord migrationDocumentStage = getTreatOfficialExtractRecord(caseId, extractionId, "Migration document", false);
                migrationDocumentStage.setError(e.getEvent().toString());
                migrationDocumentStage.setErrorMessage(e.getMessage());
                extractionStagesRepository.save(migrationDocumentStage);
                log.error("Failed creating Migration PDF for Treat Official case ID {}", caseId);
            }
        }


        //populate message
        try {
            CaseDetails message = treatOfficialMessageBuilder.buildMessage(caseId);
            message.addCaseDataItems(treatOfficialMessageCaseData.extractCaseData(caseId));
            message.setSourceCaseId(caseId.toString());
            message.setCaseAttachments(caseDetails.getCaseAttachments());
            messageService.sendMigrationMessage(message);
        } catch (ApplicationExceptions.SendMigrationMessageException e) {
            ExtractRecord correspondentStage = getTreatOfficialExtractRecord(caseId, extractionId, "Migration message", false);
            correspondentStage.setError(e.getEvent().toString());
            correspondentStage.setErrorMessage(e.getMessage());
            extractionStagesRepository.save(correspondentStage);
            log.error("Failed sending migration message for Treat Official case ID {}", caseId + " skipping case...");
            return false;
        }

        return true;
    }

    private ExtractRecord getTreatOfficialExtractRecord(BigDecimal caseId, UUID extractionId, String stage, boolean extracted) {
        ExtractRecord cer = new ExtractRecord();
        cer.setExtractionId(extractionId);
        cer.setCaseId(caseId);
        cer.setExtracted(extracted);
        cer.setStage(stage);
        return cer;
    }

}
