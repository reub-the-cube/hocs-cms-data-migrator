package uk.gov.digital.ho.hocs.cms.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@Slf4j
public class ComplaintExtractor {

    private final String COMPLAINT_ID_BY_DATE_RANGE = "SELECT caseid FROM FLODS_UKBACOMPLAINTS_D00 WHERE CREATED_DT BETWEEN :startDate AND :endDate";
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ComplaintExtractor(@Qualifier("cms-template") NamedParameterJdbcTemplate namedParametersJdbcTemplate) {
        this.jdbcTemplate = namedParametersJdbcTemplate;
    }

    public List<BigDecimal> getComplaintIdsByDateRange(String start, String end) {
        MapSqlParameterSource mapParameters = new MapSqlParameterSource();
        mapParameters.addValue("startDate", start);
        mapParameters.addValue("endDate", end);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(COMPLAINT_ID_BY_DATE_RANGE, mapParameters);
        List<BigDecimal> cases = new ArrayList<>();
        for (Map row : rows) {
                Object result = row.get("caseId");
                if (result instanceof BigDecimal bd) {
                    cases.add(bd);
                }
        }
        return  cases;
    }

    private LocalDate dateFormat(String strDate) {
        DateTimeFormatter dtf = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("MMM d,uuuu")
                .toFormatter(Locale.ENGLISH);
        return LocalDate.parse(strDate, dtf);
    }
}
