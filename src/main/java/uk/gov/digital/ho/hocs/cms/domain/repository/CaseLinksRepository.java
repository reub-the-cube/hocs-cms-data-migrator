package uk.gov.digital.ho.hocs.cms.domain.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseLinks;

import java.math.BigDecimal;
import java.util.List;

public interface CaseLinksRepository extends CrudRepository<CaseLinks, Long> {

    void deleteAllBySourceCaseId(BigDecimal caseId);

    void deleteAllByTargetCaseId(BigDecimal caseId);

    List<CaseLinks> findAllBySourceCaseId(BigDecimal caseId);

    List<CaseLinks> findAllByTargetCaseId(BigDecimal caseId);
}
