package uk.gov.digital.ho.hocs.cms.domain.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseLinks;

import java.math.BigDecimal;

public interface CaseLinksRepository extends CrudRepository<CaseLinks, Long> {

    long deleteAllBySourceCaseId(BigDecimal sourceCaseId);

    long deleteAllByTargetCaseId(BigDecimal targetCaseId);
}
