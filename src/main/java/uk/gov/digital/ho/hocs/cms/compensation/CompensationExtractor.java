package uk.gov.digital.ho.hocs.cms.compensation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.model.Compensation;
import uk.gov.digital.ho.hocs.cms.domain.repository.CompensationRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Date;

import static uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent.COMPENSATION_DATA_EXTRACTION_FAILED;

@Component
@Slf4j
public class CompensationExtractor {

    private final DataSource datasource;
    private final CompensationRepository compensationRepository;
    private final JdbcTemplate jdbcTemplate;

    public CompensationExtractor(@Qualifier("cms") DataSource datasource, CompensationRepository compensationRepository) {
        this.datasource = datasource;
        this.compensationRepository = compensationRepository;
        this.jdbcTemplate = new JdbcTemplate(datasource);
    }

    private final String FETCH_COMPENSATION_DETAILS = """
            select dateofcompensationclaim, offeraccepted, dateofpayment, compensationamount,
            amountclaimed, amountoffered, consolatorypayment
            from FLODS_UKBACOMPLAINTS_D00 where caseid = ?
            """;
    @Transactional
    public Compensation getCompensationDetails(BigDecimal caseId) {
        Compensation compensation;
        compensationRepository.deleteAllByCaseId(caseId);
        try {
            compensation = jdbcTemplate.queryForObject(FETCH_COMPENSATION_DETAILS, (rs, rowNum) -> {
                Compensation comp = new Compensation();
                comp.setDateOfCompensationClaim(convertDateToString(rs.getDate("dateofcompensationclaim")));
                comp.setOfferAccepted(rs.getString("offeraccepted"));
                comp.setDateOfPayment(convertDateToString(rs.getDate("dateofpayment")));
                comp.setCompensationAmmount(rs.getBigDecimal("compensationamount"));
                comp.setAmountClaimed(rs.getBigDecimal("amountclaimed"));
                comp.setAmountOffered(rs.getBigDecimal("amountoffered"));
                comp.setConsolatoryPayment(rs.getBigDecimal("consolatorypayment"));
                return comp;
            }, caseId);
            compensation.setCaseId(caseId);
            compensationRepository.save(compensation);
        } catch (DataAccessException e) {
            log.error("Failed extracting compensation data for Case ID: {}", caseId);
            throw new ApplicationExceptions.ExtractCompensationDataException(
                    String.format("Failed to extract compensation for case: %s", caseId), COMPENSATION_DATA_EXTRACTION_FAILED, e);
        }
        return compensation;
    }

    private String convertDateToString(Date date) {
        return (date != null) ? date.toLocalDate().toString() : "";
    }
}
