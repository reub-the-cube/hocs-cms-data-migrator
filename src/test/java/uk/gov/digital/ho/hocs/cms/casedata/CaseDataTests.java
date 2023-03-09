package uk.gov.digital.ho.hocs.cms.casedata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.jdbc.core.JdbcTemplate;

import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseData;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseDataRepository;

import javax.sql.DataSource;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CaseDataTests {

    @Mock
    private DataSource dataSource;

    @Mock
    private CaseDataRepository caseDataRepository;

    private CaseDataExtractor caseDataExtractor;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        caseDataExtractor = new CaseDataExtractor(dataSource, caseDataRepository);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    public void createCorrectCaseTypeFromCMSOwningCSUs() {
        assertEquals("COMP", CaseTypeMapping.getCaseType("CSU-Wales and South West"));
        assertEquals("COMP", CaseTypeMapping.getCaseType("CSU-Crime Directorate"));
        assertEquals("COMP", CaseTypeMapping.getCaseType("UKVI"));
        assertEquals("COMP", CaseTypeMapping.getCaseType("CSU-Immigration Enquiry Bureau"));
        assertEquals("COMP", CaseTypeMapping.getCaseType("CSU-EUSS"));
        assertEquals("COMP", CaseTypeMapping.getCaseType("CSU-NE Yorkshire and Humber"));
        assertEquals("COMP", CaseTypeMapping.getCaseType("CSU-Case Resolution Directorate"));
        assertEquals("COMP", CaseTypeMapping.getCaseType("CSU-Sheffield Call Centre"));
        assertEquals("COMP", CaseTypeMapping.getCaseType("CSU-Wales & South West"));
        assertEquals("COMP", CaseTypeMapping.getCaseType("RH-NE Ombudsman ExG"));
        assertEquals("COMP", CaseTypeMapping.getCaseType("CSU-Scotland and NI"));
        assertEquals("COMP", CaseTypeMapping.getCaseType("IE"));
        assertEquals("COMP", CaseTypeMapping.getCaseType("Surge Team"));
        assertEquals("COMP", CaseTypeMapping.getCaseType("CSU-Midland and East"));
        assertEquals("COMP", CaseTypeMapping.getCaseType("CSU-NW Region"));
        assertEquals("COMP", CaseTypeMapping.getCaseType("CSU-Criminal Casework Directorate"));
        assertEquals("COMP", CaseTypeMapping.getCaseType("CSU-London And SE"));
        assertEquals("COMP", CaseTypeMapping.getCaseType("RH-International"));
        assertEquals("COMP", CaseTypeMapping.getCaseType("Asylum Protection Hub Pilot"));

        assertEquals("BF", CaseTypeMapping.getCaseType("CSU-Border Force"));

        assertEquals("IEDET", CaseTypeMapping.getCaseType("CSU-Detention"));

        assertEquals("POGR", CaseTypeMapping.getCaseType("HMPO"));

        assertNull(CaseTypeMapping.getCaseType("UNKNOWN"));
        assertNull(CaseTypeMapping.getCaseType("NULL"));
        assertNull(CaseTypeMapping.getCaseType("other"));
    }

    private CaseData getCaseData(String owningCsu) {
        return new CaseData(
                1L,
                BigDecimal.ONE,
                "caseReferefence",
                "10/02/23",
                "10/02/23",
                "initialType",
                "currentType",
                "queueName",
                "location",
                "nroCombo",
                "closedDt",
                owningCsu,
                "businessArea",
                "status",
                "description");
    }
}