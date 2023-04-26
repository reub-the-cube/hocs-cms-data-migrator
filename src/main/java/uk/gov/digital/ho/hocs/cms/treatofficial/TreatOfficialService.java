package uk.gov.digital.ho.hocs.cms.treatofficial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import uk.gov.digital.ho.hocs.cms.complaints.ExtractResult;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.model.ComplaintExtractRecord;
import uk.gov.digital.ho.hocs.cms.domain.repository.ComplaintsRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.ProgressRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TreatOfficialService {

    private final TreatOfficialExtractor treatOfficialExtractor;
    private final ProgressRepository progressRepository;
    private final TreatOfficialCorrespondentExtractor treatOfficialCorrespondentExtractor;
    private final ComplaintsRepository complaintsRepository;
    private final ExtractResult extractResult;

    public TreatOfficialService(TreatOfficialExtractor treatOfficialExtractor,
                                TreatOfficialCorrespondentExtractor treatOfficialCorrespondentExtractor,
                                ComplaintsRepository complaintsRepository,
                                ExtractResult extractResult,
                                ProgressRepository progressRepository) {
        this.progressRepository = progressRepository;
        this.treatOfficialExtractor = treatOfficialExtractor;
        this.treatOfficialCorrespondentExtractor = treatOfficialCorrespondentExtractor;
        this.extractResult = extractResult;
        this.complaintsRepository = complaintsRepository;
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
        //extract correspondent
        try {
            treatOfficialCorrespondentExtractor.getCorrespondentsForCase(caseId);
            ComplaintExtractRecord correspondentStage = getComplaintExtractRecord(caseId, extractionId, "Correspondents", true);
            complaintsRepository.save(correspondentStage);
        } catch (ApplicationExceptions.ExtractCorrespondentException e) {
            ComplaintExtractRecord correspondentStage = getComplaintExtractRecord(caseId, extractionId, "Correspondents", false);
            correspondentStage.setError(e.getEvent().toString());
            correspondentStage.setErrorMessage(e.getMessage());
            complaintsRepository.save(correspondentStage);
            log.error("Failed extracting correspondents for case ID {}", caseId);
            return false;
        }
        return true;
    }

    private ComplaintExtractRecord getComplaintExtractRecord(BigDecimal caseId, UUID extractionId, String stage, boolean extracted) {
        ComplaintExtractRecord cer = new ComplaintExtractRecord();
        cer.setExtractionId(extractionId);
        cer.setCaseId(caseId);
        cer.setComplaintExtracted(extracted);
        cer.setStage(stage);
        return cer;
    }

}
