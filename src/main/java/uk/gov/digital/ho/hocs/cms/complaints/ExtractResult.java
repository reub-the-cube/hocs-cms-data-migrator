package uk.gov.digital.ho.hocs.cms.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.cms.domain.model.Progress;
import uk.gov.digital.ho.hocs.cms.domain.repository.ProgressRepository;

import java.util.UUID;

@Component
@Slf4j
public class ExtractResult {

    private final ProgressRepository progressRepository;

    public ExtractResult(ProgressRepository progressRepository) {
        this.progressRepository = progressRepository;

    }

    @Transactional
    public UUID saveExtractionId(int cases) {
        UUID extractionId = UUID.randomUUID();
        log.info("Extraction ID {}", extractionId);
        Progress progress = new Progress();
        progress.setExtractionId(extractionId);
        progress.setTotal(cases);
        progressRepository.save(progress);
        return extractionId;
    }

    @Transactional
    public boolean recordExtractResult(boolean result, UUID extractionId) {
        if (result) progressRepository.incrementSuccess(1, extractionId);
        else progressRepository.incrementFailure(1, extractionId);
        return result;
    }
}
