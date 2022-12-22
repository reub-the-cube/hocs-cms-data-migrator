package uk.gov.digital.ho.hocs.cms.casedata.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.cms.casedata.CaseData;

public interface CaseDataRepository  extends CrudRepository<CaseData, Long> {
}
