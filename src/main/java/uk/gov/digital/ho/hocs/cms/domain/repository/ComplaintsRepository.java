package uk.gov.digital.ho.hocs.cms.domain.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.cms.domain.model.ComplaintExtractRecord;

import java.math.BigDecimal;

@Repository
public interface ComplaintsRepository extends CrudRepository<ComplaintExtractRecord, Long> {

    @Query(value = "SELECT count(*) FROM complaints c WHERE c.complaint_extracted = false", nativeQuery = true)
    int findStageFailureForCase(BigDecimal caseId);
}
