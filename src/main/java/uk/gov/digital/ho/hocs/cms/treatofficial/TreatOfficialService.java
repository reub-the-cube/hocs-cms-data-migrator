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
            log.info("Extract a single Treat Official complaint started for complaint ID {}", treatOfficialId);
            if (extractResult.recordExtractResult(extractTreatOfficial(extractionId, treatOfficialId), extractionId)) {
                log.info("Treat Official complaint extraction for complaint ID {}, extraction ID {} finished.", treatOfficialId, extractionId);
            }
        }
        log.info("Treat Official extraction for extraction ID {} between dates {} and {} finished.", extractionId, startDate, endDate);
    }

    public void migrateTreatOfficials(List<String> complaintIds) {
        UUID extractionId = extractResult.saveExtractionId(complaintIds.size());
        for (String complaintId: complaintIds) {
            log.info("Extract a single Treat Official complaint started for complaint ID {}", complaintId);
            if (extractResult.recordExtractResult(extractTreatOfficial(extractionId, new BigDecimal(complaintId)), extractionId)) {
                log.info("Treat Official  Complaint extraction for complaint ID {}, extraction ID {} finished.", complaintId, extractionId);
            }
        }
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
