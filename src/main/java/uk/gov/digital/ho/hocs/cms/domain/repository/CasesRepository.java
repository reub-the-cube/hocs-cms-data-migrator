package uk.gov.digital.ho.hocs.cms.domain.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.cms.domain.model.ComplaintCase;
import uk.gov.digital.ho.hocs.cms.domain.model.Individual;

import java.math.BigDecimal;
import java.util.List;

public interface CasesRepository extends CrudRepository<ComplaintCase, Long> {

    void deleteAllByCaseId(BigDecimal caseId);

    ComplaintCase findByCaseId(BigDecimal caseId);

//    @Query(value = "SELECT * FROM cases WHERE case_id = ?1", nativeQuery = true)
//    ComplaintCase findIndividualsByCaseId(BigDecimal caseId);


}
