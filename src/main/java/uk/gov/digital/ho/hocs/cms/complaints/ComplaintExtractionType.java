package uk.gov.digital.ho.hocs.cms.complaints;

public enum ComplaintExtractionType {
    OPEN_CASES_ONLY("Open"),
    CLOSED_CASES_ONLY("Closed"),
    ALL_CASES(null);

    private String complaintExtractionType;

    ComplaintExtractionType(String complaintExtractionType) {
        this.complaintExtractionType = complaintExtractionType;
    }

    public String getComplaintExtractionType() {
        return complaintExtractionType;
    }

}
