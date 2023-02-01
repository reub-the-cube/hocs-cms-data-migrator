package uk.gov.digital.ho.hocs.cms.caselinks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseLinks;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseLinksRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;

import static uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent.CASE_LINKS_EXTRACTION_FAILED;

@Component
@Slf4j
public class CaseLinkExtractor {

    private final DataSource dataSource;

    private final CaseLinksRepository caseLinksRepository;

    private final JdbcTemplate jdbcTemplate;

    private final String FETCH_CASE_LINKS = """ 
            SELECT cl.SourceCaseID, cl.TargetCaseID, d.Description
            FROM lgncc_caselink cl
            INNER JOIN LGNCC_CASELINKDEFINITION d
            ON cl.LinkDefnID = d.ID
            WHERE cl.SourceCaseID = ?
             """;

    public CaseLinkExtractor(@Qualifier("cms") DataSource dataSource, CaseLinksRepository caseLinksRepository) {
        this.dataSource = dataSource;
        this.caseLinksRepository = caseLinksRepository;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    @Transactional
    public void getCaseLinks(BigDecimal sourceCaseId) {
        caseLinksRepository.deleteAllBySourceCaseId(sourceCaseId);
        try {
            CaseLinks caseLinks = jdbcTemplate.queryForObject(FETCH_CASE_LINKS, (rs, rowNum) -> {
            CaseLinks cl = new CaseLinks();
            cl.setSourceCaseId(rs.getBigDecimal("SourceCaseId"));
            cl.setTargetCaseId(rs.getBigDecimal("TargetCaseID"));
            cl.setDescription(rs.getString("Description"));
            return cl;
        }, sourceCaseId);
            caseLinksRepository.save(caseLinks);
    } catch (DataAccessException e) {
            log.error("Case links extraction failed for case ID: {}", sourceCaseId);
            throw new ApplicationExceptions.ExtractCaseLinksException(
                    String.format("Failed to extract case links for case: %s", sourceCaseId), CASE_LINKS_EXTRACTION_FAILED, e);
            }
        }
    }
