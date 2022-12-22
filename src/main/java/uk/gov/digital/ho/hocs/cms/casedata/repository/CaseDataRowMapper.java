package uk.gov.digital.ho.hocs.cms.casedata.repository;

import org.springframework.jdbc.core.RowMapper;
import uk.gov.digital.ho.hocs.cms.casedata.CaseData;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CaseDataRowMapper implements RowMapper<CaseData> {

        @Override
        public CaseData mapRow(ResultSet rs, int rowNum) throws SQLException {
            CaseData caseData = new CaseData();
            caseData.setCaseReference(rs.getString("casereference"));
            caseData.setReceiveDate(rs.getDate("ukbareceiveddate").toLocalDate());
            caseData.setSlaDate(rs.getDate("casesladate").toLocalDate());
            caseData.setInitialType(rs.getString("initialtype"));
            caseData.setCurrentType(rs.getString("currenttype"));
            caseData.setQueueName(rs.getString("queuename"));
            caseData.setLocation(rs.getString("location"));
            caseData.setNroCombo(rs.getString("nrocombo"));
            caseData.setClosedDt(rs.getDate("CLOSED_DT").toLocalDate());
            caseData.setOwningCsu(rs.getString("owningcsu"));
            caseData.setBusinessArea(rs.getString("businessarea"));
            caseData.setStatus(rs.getString("status"));
            return caseData;
        }
}
