package uk.gov.digital.ho.hocs.cms.domain.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseHistory;

import java.math.BigDecimal;
import java.util.List;

public interface CaseHistoryRepository extends CrudRepository<CaseHistory, Long> {

    void deleteAllByCaseId(BigDecimal caseId);

    List<CaseHistory> findAllByCaseId(BigDecimal caseId);
}
