package uk.gov.digital.ho.hocs.cms.domain.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseDataTreatOfficial;

import java.math.BigDecimal;

public interface CaseDataTreatOfficialsRepository  extends CrudRepository<CaseDataTreatOfficial, Long> {

    void deleteAllByCaseId(BigDecimal caseId);

    CaseDataTreatOfficial findByCaseId(BigDecimal caseId);
}