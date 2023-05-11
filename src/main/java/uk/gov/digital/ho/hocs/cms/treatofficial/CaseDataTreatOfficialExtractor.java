package uk.gov.digital.ho.hocs.cms.treatofficial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseDataTreatOfficialsRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;

@Component
@Slf4j
public class CaseDataTreatOfficialExtractor {

    private final DataSource dataSource;

    private final CaseDataTreatOfficialsRepository caseDataTreatOfficialsRepository;

    private final JdbcTemplate jdbcTemplate;

    public CaseDataTreatOfficialExtractor(@Qualifier("cms") DataSource dataSource, CaseDataTreatOfficialsRepository caseDataTreatOfficialsRepository) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.caseDataTreatOfficialsRepository = caseDataTreatOfficialsRepository;
    }

    @Transactional
    public void getCaseDataTreatOfficial(BigDecimal caseId) {

        caseDataTreatOfficialsRepository.deleteAllByCaseId(caseId);

    }
}