package uk.gov.digital.ho.hocs.cms.treatofficial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.model.ComplaintExtractRecord;
import uk.gov.digital.ho.hocs.cms.domain.model.Progress;
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

    public TreatOfficialService(TreatOfficialExtractor treatOfficialExtractor,
                                TreatOfficialCorrespondentExtractor treatOfficialCorrespondentExtractor,
                                ComplaintsRepository complaintsRepository,
                                ProgressRepository progressRepository) {
        this.progressRepository = progressRepository;
        this.treatOfficialExtractor = treatOfficialExtractor;
        this.treatOfficialCorrespondentExtractor = treatOfficialCorrespondentExtractor;
        this.complaintsRepository = complaintsRepository;
    }

    public void migrateTreatOfficials(String startDate, String endDate) {
        List<BigDecimal> treatOfficialIds = treatOfficialExtractor.getCaseIdsByDateRange(startDate, endDate);
        UUID extractionId = UUID.randomUUID();
        log.info("Extraction ID {}", extractionId);
        Progress progress = new Progress();
        progress.setExtractionId(extractionId);
        progressRepository.save(progress);
        for (BigDecimal treatOfficialId : treatOfficialIds) {
            recordExtractResult(extractTreatOfficial(extractionId, treatOfficialId), extractionId);
        }
        log.info("Treat Official extraction for extraction ID {} between dates {} and {} finished.", extractionId, startDate, endDate);
    }

    public void migrateTreatOfficial(String caseId) {
        UUID extractionId = UUID.randomUUID();
        recordExtractResult(extractTreatOfficial(extractionId, new BigDecimal(caseId)), extractionId);
        log.info("Treat Official extraction for case ID {}, extraction ID {} finished", caseId, extractionId);
    }

    private void recordExtractResult(boolean result, UUID extractionId) {
        if (result) progressRepository.incrementSuccess(1, extractionId);
        else progressRepository.incrementFailure(1, extractionId);
    }

    private boolean extractTreatOfficial(UUID extractionId, BigDecimal complaintId) {
        //extract correspondent
        try {
            treatOfficialCorrespondentExtractor.getCorrespondentsForCase(complaintId);
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
