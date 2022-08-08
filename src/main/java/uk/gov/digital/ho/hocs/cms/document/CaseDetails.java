package uk.gov.digital.ho.hocs.cms.document;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
public class CaseDetails {

    private String caseType;
    private String sourceCaseId;
    private String correspondentName;
    private String correspondenceEmail;
    private String caseStatus;
    private String caseStatusDate;
    private String creationDate;
    // getters required for Jackson marshalling
    @Getter
    private List<CaseDataItem> caseData;
    @Getter
    private List<CaseAttachment> caseAttachments;
}
