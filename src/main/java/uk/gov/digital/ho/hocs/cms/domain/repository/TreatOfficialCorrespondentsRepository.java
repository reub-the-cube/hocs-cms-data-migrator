package uk.gov.digital.ho.hocs.cms.domain.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.cms.domain.model.CorrespondentTreatOfficial;

import java.math.BigDecimal;
import java.util.List;

public interface TreatOfficialCorrespondentsRepository extends CrudRepository<CorrespondentTreatOfficial, Long> {

    void deleteAllByCaseId(BigDecimal caseId);

    List<CorrespondentTreatOfficial> findByCaseId(BigDecimal caseId);

}