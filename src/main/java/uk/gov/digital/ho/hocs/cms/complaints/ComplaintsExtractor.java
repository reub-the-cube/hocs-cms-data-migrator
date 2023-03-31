package uk.gov.digital.ho.hocs.cms.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ComplaintsExtractor {

    private final String COMPLAINT_ID_BY_DATE_RANGE_BY_STATUS = "SELECT caseid FROM FLODS_UKBACOMPLAINTS_D00 WHERE CREATED_DT BETWEEN :startDate AND :endDate AND status = :status";

    private final String COMPLAINT_ID_BY_DATE_RANGE = """
            SELECT caseid FROM FLODS_UKBACOMPLAINTS_D00
            WHERE CREATED_DT BETWEEN :startDate AND :endDate
            AND casedeleteddate IS NULL
            AND reason != 'Treat Official'
            """;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ComplaintsExtractor(@Qualifier("cms-template") NamedParameterJdbcTemplate namedParametersJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParametersJdbcTemplate;
    }

    public List<BigDecimal> getComplaintIdsByDateRange(String start, String end, ComplaintExtractionType extractionType) {
        String complaintExtractionQuery;
        MapSqlParameterSource mapParameters = new MapSqlParameterSource();
        mapParameters.addValue("startDate", start);
        mapParameters.addValue("endDate", end);

        complaintExtractionQuery = getExtractionQuery(extractionType, mapParameters);

        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(complaintExtractionQuery, mapParameters);

        log.info("{} complaint cases selected between {} and {}", rows.size(), start, end);
        List<BigDecimal> cases = new ArrayList<>();
        for (Map row : rows) {
            Object result = row.get("caseId");
            if (result instanceof BigDecimal bd) {
                cases.add(bd);
            }
        }
        return  cases;
    }

    String getExtractionQuery(ComplaintExtractionType extractionType, MapSqlParameterSource mapParameters) {
        String complaintExtractionQuery;
        if (isSingleExtractionType(extractionType)) {
            mapParameters.addValue("status", getExtractionType(extractionType));
            complaintExtractionQuery = COMPLAINT_ID_BY_DATE_RANGE_BY_STATUS;
        } else {
            complaintExtractionQuery = COMPLAINT_ID_BY_DATE_RANGE;
        }
        return complaintExtractionQuery;
    }

    private boolean isSingleExtractionType(ComplaintExtractionType extractionType) {
        switch(extractionType){
            case OPEN_CASES_ONLY:
            case CLOSED_CASES_ONLY:
                return true;
            default:
                return false;
        }
    }

    private String getExtractionType(ComplaintExtractionType extractionType) {
        return extractionType.getComplaintExtractionType();
    }
}
