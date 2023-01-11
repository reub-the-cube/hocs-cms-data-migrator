package uk.gov.digital.ho.hocs.cms.domain.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.cms.domain.model.Reference;

@Repository
public interface ReferenceRepository extends CrudRepository<Reference, Long> {
}
