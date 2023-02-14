package uk.gov.digital.ho.hocs.cms.domain.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    public void addCaseAttachment(CaseAttachment caseAttachment) {
        if (caseAttachments == null) {
            caseAttachments = new ArrayList<>();
        }
        caseAttachments.add(caseAttachment);
    }
}
