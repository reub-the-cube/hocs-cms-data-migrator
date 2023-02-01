package uk.gov.digital.ho.hocs.cms.response;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.model.Response;
import uk.gov.digital.ho.hocs.cms.domain.repository.ResponseRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;

import static uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent.RESPONSE_EXTRACTION_FAILED;

@Component
@Slf4j
public class ResponseExtractor {
    private final ResponseRepository responseRepository;

    private final DataSource dataSource;

    private final JdbcTemplate jdbcTemplate;

    private final String FETCH_CASE_LINKS = "SELECT QualityAssurance FROM FLODS_UKBACOMPLAINTS_D00 WHERE caseid = ?";

    public ResponseExtractor(@Qualifier("cms") DataSource dataSource,
                             ResponseRepository responseRepository) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.responseRepository = responseRepository;
    }

    @Transactional
    public void getResponse(BigDecimal caseId) {
        responseRepository.deleteAllByCaseId(caseId);
        try {
            Response response = jdbcTemplate.queryForObject(FETCH_CASE_LINKS, (rs, rowNum) -> {
                Response r = new Response();
                r.setResponse(rs.getString("QualityAssurance"));
                return r;
        }, caseId);
            response.setCaseId(caseId);
            responseRepository.save(response);
    } catch (DataAccessException e) {
            log.error("Failed extracting response for case ID: {} Error: {}", caseId, e.getMessage());
            throw new ApplicationExceptions.ExtractResponseException(
                    String.format("Failed to extract response for case: %s", e.getMessage()), RESPONSE_EXTRACTION_FAILED);
            }
        }
    }
