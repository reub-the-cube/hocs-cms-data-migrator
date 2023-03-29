package uk.gov.digital.ho.hocs.cms.complaints;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.junit.Assert.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ComplaintsExtractorTests {

    private ComplaintsExtractor complaintsExtractor;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final String SINGLE_EXTRACTION_TYPE_COMPLAINT_ID_BY_DATE_RANGE = "SELECT caseid FROM FLODS_UKBACOMPLAINTS_D00 WHERE CREATED_DT BETWEEN :startDate AND :endDate AND status = :status";
    private final String ALL_COMPLAINTS_ID_BY_DATE_RANGE = "SELECT caseid FROM FLODS_UKBACOMPLAINTS_D00 WHERE CREATED_DT BETWEEN :startDate AND :endDate";


    @BeforeEach
    void setUp() {
        complaintsExtractor = new ComplaintsExtractor(namedParameterJdbcTemplate);
    }

    @Test
    public void testExtractionQueryForOpenOrClosedComplaints() {
        String start = "2020-01-01";
        String end = "2023-12-12";

        MapSqlParameterSource mapParameters = new MapSqlParameterSource();
        mapParameters.addValue("startDate", start);
        mapParameters.addValue("endDate", end);

        String resultQuery = complaintsExtractor.getExtractionQuery(ComplaintExtractionType.CLOSED_CASES_ONLY, mapParameters);
        assertEquals(SINGLE_EXTRACTION_TYPE_COMPLAINT_ID_BY_DATE_RANGE, resultQuery);

        resultQuery = complaintsExtractor.getExtractionQuery(ComplaintExtractionType.OPEN_CASES_ONLY, mapParameters);
        assertEquals(SINGLE_EXTRACTION_TYPE_COMPLAINT_ID_BY_DATE_RANGE, resultQuery);
    }

    @Test
    public void testExtractionQueryForBothOpenAndClosedComplaints() {
        String start = "2020-01-01";
        String end = "2023-12-12";

        MapSqlParameterSource mapParameters = new MapSqlParameterSource();
        mapParameters.addValue("startDate", start);
        mapParameters.addValue("endDate", end);

        String resultQuery = complaintsExtractor.getExtractionQuery(ComplaintExtractionType.ALL_CASES, mapParameters);
        assertEquals(ALL_COMPLAINTS_ID_BY_DATE_RANGE, resultQuery);
    }
}
