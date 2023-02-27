package uk.gov.digital.ho.hocs.cms.domain.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.cms.domain.model.Categories;

import java.math.BigDecimal;
import java.util.List;

public interface CategoriesRepository extends CrudRepository<Categories, Long> {

    void deleteAllByCaseId(BigDecimal caseId);

    List<Categories> findAllByCaseId(BigDecimal caseId);
}
