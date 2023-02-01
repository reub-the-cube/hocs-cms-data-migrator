package uk.gov.digital.ho.hocs.cms.domain.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.cms.domain.model.Categories;

import java.math.BigDecimal;

public interface CategoriesRepository extends CrudRepository<Categories, Long> {

    long deleteAllByCaseId(BigDecimal caseId);
}
