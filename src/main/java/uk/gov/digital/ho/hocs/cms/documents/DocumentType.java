package uk.gov.digital.ho.hocs.cms.documents;

import lombok.Getter;

public enum DocumentType {
    MIGRATION ("Migration document");

    @Getter
    private final String label;

    DocumentType(String value) {
        this.label = value;
    }

    public static DocumentType valueOfLabel(String label) {
        for (DocumentType dt : values()) {
            if (dt.label.equals(label)) {
                return dt;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
