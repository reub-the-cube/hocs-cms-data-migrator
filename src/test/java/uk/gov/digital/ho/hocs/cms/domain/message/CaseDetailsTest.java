package uk.gov.digital.ho.hocs.cms.domain.message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class CaseDetailsTest {

    private CaseDetails caseDetails;

    final String DOCUMENT_TYPE = "document type";
    final String DOCUMENT_NAME = "document name";
    final String DOCUMENT_PATH = "document path";


    @BeforeEach
    public void init() {
        caseDetails = new CaseDetails();
    }

    @Test
    public void testAddCaseAttachment() {
        CaseAttachment caseAttachment = new CaseAttachment();
        caseAttachment.setDocumentType(DOCUMENT_TYPE);
        caseAttachment.setDocumentPath(DOCUMENT_PATH);
        caseAttachment.setDisplayName(DOCUMENT_NAME);
        caseDetails.addCaseAttachments(caseAttachment);
        assertNotNull(caseDetails.getCaseAttachments());
        assertEquals(1, caseDetails.getCaseAttachments().size());
    }

    @Test
    public void testAddCaseAttachments() {
        List<CaseAttachment> caseAttachments = new ArrayList<>();
        CaseAttachment caseAttachment = new CaseAttachment();
        caseAttachment.setDocumentType(DOCUMENT_TYPE);
        caseAttachment.setDocumentPath(DOCUMENT_PATH);
        caseAttachment.setDisplayName(DOCUMENT_NAME + "1");
        caseAttachments.add(caseAttachment);
        CaseAttachment caseAttachment2 = new CaseAttachment();
        caseAttachment2.setDocumentType(DOCUMENT_TYPE);
        caseAttachment2.setDocumentPath(DOCUMENT_PATH);
        caseAttachment2.setDisplayName(DOCUMENT_NAME + "2");
        caseAttachments.add(caseAttachment2);
        caseDetails.addCaseAttachments(caseAttachments);
        assertEquals(2, caseDetails.getCaseAttachments().size());
    }

}