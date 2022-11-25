package uk.gov.digital.ho.hocs.cms.message;

import lombok.Builder;
import lombok.Getter;
import uk.gov.digital.ho.hocs.cms.message.CaseAttachment;
import uk.gov.digital.ho.hocs.cms.message.CaseDataItem;

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
    private List<CaseDataItem> caseData;
    private List<CaseAttachment> caseAttachments;
}
