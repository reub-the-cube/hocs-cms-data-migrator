package uk.gov.digital.ho.hocs.cms.domain.message;

import org.junit.jupiter.api.Test;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


public class CaseDetailsTest {

    @Test
    public void shouldAddCaseAttachment() {
        String displayName = "Display Name";
        String documentPath = "Document Path";
        String documentType = "Document Type";
        CaseDetails caseDetails = new CaseDetails();
        CaseAttachment caseAttachment = new CaseAttachment();
        caseAttachment.setDisplayName(displayName);
        caseAttachment.setDocumentPath(documentPath);
        caseAttachment.setDocumentType(documentType);
        caseDetails.addCaseAttachment(caseAttachment);
        List<CaseAttachment> addedCaseAttachments = caseDetails.getCaseAttachments();
        if (addedCaseAttachments.size() != 1) {
            fail("There isn't a single Case Attachment.");
        }
        CaseAttachment addedCaseAttachment = addedCaseAttachments.get(0);
        assertThat(addedCaseAttachment.getDisplayName()).isEqualTo(displayName);
        assertThat(addedCaseAttachment.getDocumentPath()).isEqualTo(documentPath);
        assertThat(addedCaseAttachment.getDocumentType()).isEqualTo(documentType);
    }
}