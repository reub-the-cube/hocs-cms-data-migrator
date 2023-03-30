package uk.gov.digital.ho.hocs.cms.domain.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.cms.domain.model.Progress;

import java.util.UUID;

public interface ProgressRepository extends CrudRepository<Progress, Long> {

    Progress findByExtractionId(UUID extractionId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Progress p set p.success = p.success + :sentIncrement WHERE p.extractionId = :extractionId")
    void incrementSuccess(long sentIncrement, UUID extractionId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Progress p set p.failure = p.failure + :sentIncrement WHERE p.extractionId = :extractionId")
    void incrementFailure(long sentIncrement, UUID extractionId);

}
