package uk.gov.digital.ho.hocs.cms.documents;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentTypeTest {

    @Test
    public void testValue() {
        assertSame(DocumentType.MIGRATION, DocumentType.valueOfLabel("Migration document"));
    }

    @Test
    public void testToString() {
        assertSame(DocumentType.MIGRATION.getLabel(), DocumentType.MIGRATION.toString());
    }

}