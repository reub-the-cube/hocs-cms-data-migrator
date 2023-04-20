package uk.gov.digital.ho.hocs.cms.history;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseHistory;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseHistoryRepository;
import uk.gov.digital.ho.hocs.cms.utils.CharacterDecoder;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.nio.charset.CharacterCodingException;
import java.sql.Date;
import java.util.List;

import static uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent.CASE_HISTORY_EXTRACTION_FAILED;

@Component
@Slf4j
public class CaseHistoryExtractor {

    private final DataSource dataSource;

    private final JdbcTemplate jdbcTemplate;

    private final CaseHistoryRepository caseHistoryRepository;

    private final CharacterDecoder characterDecoder;

    private final String FETCH_CASE_HISTORY = """ 
        select CASEID , LINE1 , LINE2 , CREATEDBY , CREATIONDATE from LGNCC_CASEEVENTVIEW lc where CASEID = ? UNION
        select CASEID , LINE1 , LINE2 , CREATEDBY , CREATIONDATE from LGNCC_CASENOTEVIEW lc where CASEID = ? order by CREATIONDATE;
             """;

    public CaseHistoryExtractor(@Qualifier("cms") DataSource dataSource, CaseHistoryRepository caseHistoryRepository,
                                CharacterDecoder characterDecoder) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.caseHistoryRepository = caseHistoryRepository;
        this.characterDecoder = characterDecoder;
    }

    @Transactional
    public void getCaseHistory(BigDecimal caseId) {
        caseHistoryRepository.deleteAllByCaseId(caseId);
        try {
            List<CaseHistory> caseHistory = jdbcTemplate.query(FETCH_CASE_HISTORY, (rs, rowNum) -> {
                CaseHistory ch = new CaseHistory();
                ch.setCaseId(rs.getBigDecimal("CASEID"));
                ch.setType(characterDecoder.decodeWindows1252Charset(rs.getBytes("LINE1")));
                ch.setDescription(characterDecoder.decodeWindows1252Charset(rs.getBytes("LINE2")));
                ch.setCreatedBy(rs.getString("CREATEDBY"));
                ch.setCreated(rs.getDate("CREATIONDATE"));
                return ch;
            }, caseId, caseId);
            persistExtractedCaseHistory(caseHistory);
        } catch (DataAccessException e) {
            log.error("Case history extraction failed for case ID: {}", caseId);
            throw new ApplicationExceptions.ExtractCaseHistoryException(e.getMessage(), CASE_HISTORY_EXTRACTION_FAILED, e);
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
