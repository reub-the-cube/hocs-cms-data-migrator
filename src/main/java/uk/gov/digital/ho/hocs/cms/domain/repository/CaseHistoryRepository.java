package uk.gov.digital.ho.hocs.cms.domain.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseHistory;

import java.math.BigDecimal;

public interface CaseHistoryRepository extends CrudRepository<CaseHistory, Long> {

    long deleteAllByCaseId(BigDecimal caseId);
}
