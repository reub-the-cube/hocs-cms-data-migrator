package uk.gov.digital.ho.hocs.cms.domain.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.cms.domain.model.Response;

import java.math.BigDecimal;

public interface ResponseRepository extends CrudRepository<Response, Long> {

    void deleteAllByCaseId(BigDecimal caseId);

    Response findByCaseId(BigDecimal caseId);
}
