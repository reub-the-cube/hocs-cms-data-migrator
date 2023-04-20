package uk.gov.digital.ho.hocs.cms.risk;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.model.RiskAssessment;
import uk.gov.digital.ho.hocs.cms.domain.repository.RiskAssessmentRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;

import static uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent.RISK_ASSESSMENT_EXTRACTION_FAILED;

@Component
@Slf4j
public class RiskAssessmentExtractor {

    private final DataSource dataSource;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final JdbcTemplate jdbcTemplate;

    private final String FETCH_RISK_ASSESSMENT = "select priority, fromoraffectingachild from FLODS_UKBACOMPLAINTS_D00 where caseid = ?";

    public RiskAssessmentExtractor(@Qualifier("cms") DataSource dataSource, RiskAssessmentRepository riskAssessmentRepository, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Transactional
    public void getRiskAssessment(BigDecimal caseId) {
        riskAssessmentRepository.deleteAllByCaseId(caseId);
        try {
            RiskAssessment riskAssessment = jdbcTemplate.queryForObject(FETCH_RISK_ASSESSMENT, (rs, rowNum) -> {
                RiskAssessment ra = new RiskAssessment();
                ra.setPriority(rs.getString("priority"));
                ra.setFromOrAffectingAChild(rs.getString("fromoraffectingachild"));
                return ra;
            }, caseId);
            riskAssessment.setCaseId(caseId);
            riskAssessmentRepository.save(riskAssessment);
        } catch (DataAccessException e) {
            log.error("Failed extracting risk assessment for Case ID: {}", caseId);
            throw new ApplicationExceptions.ExtractCompensationDataException(
                    String.format("Failed to extract risk assessment for case: %s", caseId), RISK_ASSESSMENT_EXTRACTION_FAILED, e);
        }
    }
}
