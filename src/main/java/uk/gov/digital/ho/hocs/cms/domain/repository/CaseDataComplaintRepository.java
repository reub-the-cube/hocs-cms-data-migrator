package uk.gov.digital.ho.hocs.cms.domain.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseDataComplaint;

import java.math.BigDecimal;

public interface CaseDataComplaintRepository extends CrudRepository<CaseDataComplaint, Long> {

    void deleteAllByCaseId(BigDecimal caseId);

    CaseDataComplaint findByCaseId(BigDecimal caseId);
}
