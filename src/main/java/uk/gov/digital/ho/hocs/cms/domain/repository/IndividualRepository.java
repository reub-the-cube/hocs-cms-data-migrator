package uk.gov.digital.ho.hocs.cms.domain.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.cms.domain.model.Individual;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface IndividualRepository extends CrudRepository<Individual, BigDecimal> {

    @Query(value = "SELECT partyid FROM individual WHERE caseid = ?1", nativeQuery = true)
    List<BigDecimal> findIndividualsByCaseId(BigDecimal caseid);
}
