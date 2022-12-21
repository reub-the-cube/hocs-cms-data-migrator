package uk.gov.digital.ho.hocs.cms.domain.message;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class CaseDetails {

    private String caseType;
    private String sourceCaseId;
    private Correspondent primaryCorrespondent;
    // getters required for Jackson marshalling
    @Getter
    private List<Correspondent> additionalCorrespondents;
    private String caseStatus;
    private String caseStatusDate;
    private String creationDate;
    @Getter
    private List<CaseDataItem> caseData;
    @Getter
    private List<CaseAttachment> caseAttachments;
}
