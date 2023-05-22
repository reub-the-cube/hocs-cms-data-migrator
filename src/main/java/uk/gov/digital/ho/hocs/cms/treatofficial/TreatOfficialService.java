package uk.gov.digital.ho.hocs.cms.treatofficial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import uk.gov.digital.ho.hocs.cms.complaints.ExtractResult;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseDetails;
import uk.gov.digital.ho.hocs.cms.domain.model.ExtractRecord;
import uk.gov.digital.ho.hocs.cms.domain.repository.ExtractionStagesRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.ProgressRepository;

import java.math.BigDecimal;
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
    private final ExtractResult extractResult;

    public TreatOfficialService(TreatOfficialExtractor treatOfficialExtractor,
                                TreatOfficialCorrespondentExtractor treatOfficialCorrespondentExtractor,
                                CaseDataTreatOfficialExtractor caseDataTreatOfficialExtractor,
                                ExtractionStagesRepository extractionStagesRepository,
                                TreatOfficialMessageBuilder treatOfficialMessageBuilder,
                                ExtractResult extractResult,
                                ProgressRepository progressRepository) {
        this.progressRepository = progressRepository;
        this.treatOfficialExtractor = treatOfficialExtractor;
        this.treatOfficialCorrespondentExtractor = treatOfficialCorrespondentExtractor;
        this.caseDataTreatOfficialExtractor = caseDataTreatOfficialExtractor;
        this.extractResult = extractResult;
        this.extractionStagesRepository = extractionStagesRepository;
        this.treatOfficialMessageBuilder = treatOfficialMessageBuilder;
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
            log.error("Failed extracting case data treat official for case ID {}", caseId);
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
            log.error("Failed extracting correspondents for case ID {}", caseId);
            return false;
        }

        //populate message
        try {
            CaseDetails message = treatOfficialMessageBuilder.buildMessage(caseId);

        } catch (ApplicationExceptions.SendMigrationMessageException e) {

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
