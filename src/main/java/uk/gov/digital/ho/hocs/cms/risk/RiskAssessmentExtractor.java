package uk.gov.digital.ho.hocs.cms.risk;

import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.digital.ho.hocs.cms.risk.repository.RiskAssessmentRowMapper;

import javax.sql.DataSource;
import java.math.BigDecimal;

public class RiskAssessmentExtractor {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    private final String FETCH_RISK_ASSESSMENT = "select priority, fromoraffectingachild from FLODS_UKBACOMPLAINTS_D00 where caseid = :caseId";

    public RiskAssessmentExtractor(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void getRiskAssessment(BigDecimal caseId) {
        RiskAssessment riskAssessment = jdbcTemplate.queryForObject(FETCH_RISK_ASSESSMENT, new RiskAssessmentRowMapper(), caseId);
    }
}
