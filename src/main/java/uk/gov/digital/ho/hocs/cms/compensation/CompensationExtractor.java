package uk.gov.digital.ho.hocs.cms.compensation;

import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.digital.ho.hocs.cms.compensation.repository.CompensationRowMapper;

import javax.sql.DataSource;
import java.math.BigDecimal;

public class CompensationExtractor {

    private final DataSource datasource;
    private final JdbcTemplate jdbcTemplate;

    public CompensationExtractor(DataSource datasource) {
        this.datasource = datasource;
        this.jdbcTemplate = new JdbcTemplate(datasource);
    }

    private final String FETCH_COMPENSATION_DETAILS = """
            select dateofcompensationclaim, offeraccepted, dateofpayment, compensationamount,
            amountclaimed, amountoffered, consolatorypayment
            from FLODS_UKBACOMPLAINTS_D00
            """;

    public Compensation getCompensationDetails(BigDecimal caseId) {
        Compensation compensation = jdbcTemplate.queryForObject(FETCH_COMPENSATION_DETAILS, new CompensationRowMapper(), caseId);
        return compensation;
    }
}
