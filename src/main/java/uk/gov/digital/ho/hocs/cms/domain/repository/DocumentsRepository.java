package uk.gov.digital.ho.hocs.cms.domain.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.cms.domain.model.DocumentExtractRecord;

import java.math.BigDecimal;

@Repository
public interface DocumentsRepository extends CrudRepository<DocumentExtractRecord, Long> {

    void deleteAllByCaseId(BigDecimal caseId);

    @Query(value = "SELECT count(*) FROM documents d WHERE d.case_id = ?1 AND d.document_extracted = 'false'", nativeQuery = true)
    BigDecimal findFailedDocumentsForCase(BigDecimal caseId);
}
