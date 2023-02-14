package uk.gov.digital.ho.hocs.cms.domain.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseData;

import java.math.BigDecimal;

public interface CaseDataRepository  extends CrudRepository<CaseData, Long> {

    void deleteAllByCaseId(BigDecimal caseId);

    CaseData findByCaseId(BigDecimal caseId);
}
