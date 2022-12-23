package uk.gov.digital.ho.hocs.cms.risk.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.cms.risk.RiskAssessment;

public interface RiskAssessmentRepository extends CrudRepository<RiskAssessment, Long> {
}
