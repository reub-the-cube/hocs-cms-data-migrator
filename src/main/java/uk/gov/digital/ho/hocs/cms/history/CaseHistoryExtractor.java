package uk.gov.digital.ho.hocs.cms.history;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseHistory;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseLinks;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseHistoryRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import static uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent.CASE_LINKS_EXTRACTION_FAILED;

@Component
@Slf4j
public class CaseHistoryExtractor {

    private final DataSource dataSource;

    private final JdbcTemplate jdbcTemplate;

    private final CaseHistoryRepository caseHistoryRepository;

    private final String FETCH_CASE_HISTORY = """ 
           SELECT cev.CASEID, cev.LINE1, cev.LINE2, cev.CREATEDBY, cev.CREATIONDATE from LGNCC_CASENOTEVIEW cnv 
           INNER JOIN LGNCC_CASEEVENTVIEW cev on cnv.CASEID = cev.CASEID
           WHERE cev.CASEID = ?
             """;

    public CaseHistoryExtractor(@Qualifier("cms") DataSource dataSource, CaseHistoryRepository caseHistoryRepository) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.caseHistoryRepository = caseHistoryRepository;
    }

    @Transactional
    public void getCaseHistory(BigDecimal caseId) {
        caseHistoryRepository.deleteAllByCaseId(caseId);
        try {
            List<CaseHistory> caseHistory = jdbcTemplate.query(FETCH_CASE_HISTORY, (rs, rowNum) -> {
            CaseHistory ch = new CaseHistory();
            ch.setCaseId(rs.getBigDecimal("CASEID"));
            ch.setType(rs.getString("LINE1"));
            ch.setDescription(rs.getString("LINE2"));
            ch.setCreatedBy(rs.getString("CREATEDBY"));
            ch.setCreated(rs.getDate("CREATIONDATE"));
            return ch;
        }, caseId);
            persistExtractedCaseHistory(caseHistory);
    } catch (DataAccessException e) {
            log.error("Case links extraction failed for case ID: {}", caseId);
            throw new ApplicationExceptions.ExtractCaseHistoryException(
                    String.format("Failed to extract case links for case: %s", caseId), CASE_LINKS_EXTRACTION_FAILED, e);
            }
        }

    private void persistExtractedCaseHistory(List<CaseHistory> caseHistory) {
        for (CaseHistory ch : caseHistory) {
            caseHistoryRepository.save(ch);
        }
    }

    private String convertDateToString(Date date) {
        return (date != null) ? date.toLocalDate().toString() : "";
    }

}