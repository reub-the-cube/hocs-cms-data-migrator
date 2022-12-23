package uk.gov.digital.ho.hocs.cms.risk.repository;

import org.springframework.jdbc.core.RowMapper;
import uk.gov.digital.ho.hocs.cms.risk.RiskAssessment;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RiskAssessmentRowMapper implements RowMapper<RiskAssessment> {

    @Override
    public RiskAssessment mapRow(ResultSet rs, int rowNum) throws SQLException {
        RiskAssessment riskAssessment = new RiskAssessment();
        riskAssessment.setPriority(rs.getString("priority"));
        riskAssessment.setFromOrAffectingAChild(rs.getString("fromoraffectingachild"));
        return riskAssessment;
    }
}
