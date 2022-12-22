package uk.gov.digital.ho.hocs.cms.compensation.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.cms.compensation.Compensation;

public interface CompensationRepository extends CrudRepository<Compensation, Long> {
}
