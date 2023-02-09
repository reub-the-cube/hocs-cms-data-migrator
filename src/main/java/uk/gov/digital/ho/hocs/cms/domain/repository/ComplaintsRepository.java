package uk.gov.digital.ho.hocs.cms.domain.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.cms.domain.model.ComplaintExtractRecord;

import java.math.BigDecimal;

@Repository
public interface ComplaintsRepository extends CrudRepository<ComplaintExtractRecord, BigDecimal> {
}
