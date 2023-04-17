package uk.gov.digital.ho.hocs.cms.treatofficial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@Component
@Slf4j
public class TreatOfficialExtractor {

    private final String TREAT_OFFICIAL_CASE_ID_BY_DATE_RANGE = """
            SELECT * from LGNCC_CLOSEDCASEVIEW
            inner join LGNCC_ENQUIRY on LGNCC_CLOSEDCASEVIEW.CaseId = LGNCC_ENQUIRY.CaseID
            WHERE
            LGNCC_CLOSEDCASEVIEW.ReasonName = 'Treat Official' AND
            LGNCC_CLOSEDCASEVIEW.xref1 is not null AND
            lgncc_enquiry.deleteddate is null AND
            LGNCC_CLOSEDCASEVIEW.OpenedDateTime BETWEEN :startDate AND :endDate
            """;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public TreatOfficialExtractor(@Qualifier("cms-template") NamedParameterJdbcTemplate namedParametersJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParametersJdbcTemplate;
    }

    public List<BigDecimal> getCaseIdsByDateRange(String start, String end) {
        MapSqlParameterSource mapParameters = new MapSqlParameterSource();
        mapParameters.addValue("startDate", start);
        mapParameters.addValue("endDate", end);

        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(TREAT_OFFICIAL_CASE_ID_BY_DATE_RANGE, mapParameters);
        log.info("{} Treat Official cases selected between {} and {}", rows.size(), start, end);

        List<BigDecimal> cases = new ArrayList<>();
        for (Map row : rows) {
            Object result = row.get("caseId");
            if (result instanceof BigDecimal bd) {
                cases.add(bd);
            }
        }
        return  cases;
    }

}
