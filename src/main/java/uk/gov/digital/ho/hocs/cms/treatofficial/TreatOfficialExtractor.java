package uk.gov.digital.ho.hocs.cms.treatofficial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

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
        return Collections.emptyList();
    }

}
