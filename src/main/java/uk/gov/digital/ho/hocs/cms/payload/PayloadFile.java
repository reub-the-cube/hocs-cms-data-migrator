package uk.gov.digital.ho.hocs.cms.payload;

import lombok.Getter;

@Getter
public enum PayloadFile {
    BIOMETRIC("templates/biometric.json"),
    DECISION("templates/decision.json"),
    DELAYS("templates/delays.json"),
    EXISTING("templates/existing.json"),
    MAKING_APPOINTMENT("templates/makingAppointment.json"),
    POOR_INFORMATION("templates/poorInformation.json"),
    REFUND("templates/refund.json"),
    SOMETHING_ELSE("templates/somethingElse.json"),
    STAFF_BEHAVIOUR("templates/staffBehaviour.json"),
    SUBMITTING_APPLICATION("templates/submittingApplication.json");

    private final String fileName;

    PayloadFile(String fileName) {
        this.fileName = fileName;
    }
}
