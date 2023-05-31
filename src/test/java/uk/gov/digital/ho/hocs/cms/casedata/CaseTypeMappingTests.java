package uk.gov.digital.ho.hocs.cms.casedata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.Assert.assertEquals;

@ExtendWith(MockitoExtension.class)
public class CaseTypeMappingTests {

    @Test
    public void createCorrectCaseTypeForNullorUnknownFromQueueName() {
        assertEquals("COMP", QueueNameMapping.getCaseType("BPM"));
        assertEquals("COMP", QueueNameMapping.getCaseType("A template reply"));
        assertEquals("COMP", QueueNameMapping.getCaseType("DNA Test Fees"));
        assertEquals("COMP", QueueNameMapping.getCaseType("Minor Misconduct"));
        assertEquals("COMP", QueueNameMapping.getCaseType("Priority Allocations CCT"));
        assertEquals("COMP", QueueNameMapping.getCaseType("PSU"));
        assertEquals("COMP", QueueNameMapping.getCaseType("UNALLOCATED-Minor Misconduct"));
        assertEquals("COMP", QueueNameMapping.getCaseType("UNALLOCATED-Serious Misconduct"));
        assertEquals("COMP", QueueNameMapping.getCaseType("UNALLOCATED-Service"));
        assertEquals("COMP", QueueNameMapping.getCaseType(null));
        assertEquals("POGR", QueueNameMapping.getCaseType("HMPO"));
    }
}
