package uk.gov.digital.ho.hocs.cms.domain.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.cms.domain.model.RiskAssessment;

import java.math.BigDecimal;

public interface RiskAssessmentRepository extends CrudRepository<RiskAssessment, Long> {

    void deleteAllByCaseId(BigDecimal caseId);

    RiskAssessment findByCaseId(BigDecimal caseId);
}
