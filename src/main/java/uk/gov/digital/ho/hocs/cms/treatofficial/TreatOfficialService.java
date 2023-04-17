package uk.gov.digital.ho.hocs.cms.treatofficial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.cms.domain.model.Progress;
import uk.gov.digital.ho.hocs.cms.domain.repository.ProgressRepository;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class TreatOfficialService {

    private final TreatOfficialExtractor treatOfficialExtractor;
    private final ProgressRepository progressRepository;

    public TreatOfficialService(TreatOfficialExtractor treatOfficialExtractor,
                                ProgressRepository progressRepository) {
        this.progressRepository = progressRepository;
        this.treatOfficialExtractor = treatOfficialExtractor;
    }

    @Transactional
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

    @Transactional
    public void migrateTreatOfficial(String caseId) {
        UUID extractionId = UUID.randomUUID();
        recordExtractResult(extractTreatOfficial(extractionId, new BigDecimal(caseId)), extractionId);
        log.info("Treat Official extraction for case ID {}, extraction ID {} finished", caseId, extractionId);
    }

    private void recordExtractResult(boolean result, UUID extractionId) {
        if (result) progressRepository.incrementSuccess(1, extractionId);
        else progressRepository.incrementFailure(1, extractionId);
    }

    private boolean extractTreatOfficial(UUID extractionId, BigDecimal caseId) {
        return false;
    }

}
