package uk.gov.digital.ho.hocs.cms.treatofficial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseDataTreatOfficial;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseDataTreatOfficialsRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Date;

@Component
@Slf4j
public class CaseDataTreatOfficialExtractor {

    private final DataSource dataSource;

    private final CaseDataTreatOfficialsRepository caseDataTreatOfficialsRepository;

    private final JdbcTemplate jdbcTemplate;

    private final String FETCH_CASE_DATA_CLOSEDCASEVIEW = "select OpenedDateTime, TypeID, allocatedToDeptID, CaseRef, targetFixDateTime, otherDescription, Title, closedDateTime, Severity, Priority, CaseStatus from LGNCC_CLOSEDCASEVIEW where CaseId = ?";

    private final String FETCH_CASE_DATA_TREATOFFICIALEFORM = """
            select letterTopic, ResponseDate, tx_rejectnotes
            from LGNES_TreatOfficialEform where caseid = ?
            """;

    public CaseDataTreatOfficialExtractor(@Qualifier("cms") DataSource dataSource, CaseDataTreatOfficialsRepository caseDataTreatOfficialsRepository) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.caseDataTreatOfficialsRepository = caseDataTreatOfficialsRepository;
    }

    @Transactional
    public void getCaseDataTreatOfficial(BigDecimal caseId) {

        caseDataTreatOfficialsRepository.deleteAllByCaseId(caseId);

        CaseDataTreatOfficial caseDataTreatOfficial = jdbcTemplate.queryForObject(FETCH_CASE_DATA_CLOSEDCASEVIEW, (rs, rowNum) -> {
            CaseDataTreatOfficial caseData = new CaseDataTreatOfficial();
            caseData.setOpenedDateTime(convertDateToString(rs.getDate("OpenedDateTime")));
            caseData.setTypeId(rs.getString("TypeID"));
            caseData.setAllocatedToDeptId(rs.getString("allocatedToDeptID"));
            caseData.setCaseRef(rs.getString("CaseRef"));
            caseData.setTargetFixDateTime(convertDateToString(rs.getDate("targetFixDateTime")));
            caseData.setOtherDescription(rs.getString("otherDescription"));
            caseData.setTitle(rs.getString("Title"));
            caseData.setClosedDateTime(convertDateToString(rs.getDate("closedDateTime")));
            caseData.setSeverity(rs.getBigDecimal("Severity"));
            caseData.setPriority(rs.getBigDecimal("Priority"));
            caseData.setStatus(rs.getBigDecimal("CaseStatus"));

            return caseData;
        }, caseId);

        caseDataTreatOfficial.setCaseId(caseId);
        caseDataTreatOfficialsRepository.save(caseDataTreatOfficial);
    }

    private String convertDateToString(Date date) {
        return (date != null) ? date.toLocalDate().toString() : "";
    }
}