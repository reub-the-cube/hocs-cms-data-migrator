package uk.gov.digital.ho.hocs.cms.domain.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.cms.domain.model.Compensation;

import java.math.BigDecimal;

public interface CompensationRepository extends CrudRepository<Compensation, Long> {

    void deleteAllByCaseId(BigDecimal caseId);

    Compensation findByCaseId(BigDecimal caseId);
}
