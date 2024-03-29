package uk.gov.digital.ho.hocs.cms.domain.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
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

    private String dateReceived;
    private String deadlineDate;

    public void addCaseDataIte(CaseDataItem cdi) {
        if (caseData == null) {
            caseData = new ArrayList<>();
        }
        caseData.add(cdi);
    }

    public void addCaseDataItems(List<CaseDataItem> cdi) {
        if (caseData == null) {
            caseData = new ArrayList<>();
        }
        caseData.addAll(cdi);
    }

    public void addCaseAttachment(CaseAttachment ca) {
        if (caseAttachments == null) {
            caseAttachments = new ArrayList<>();
        }
        caseAttachments.add(ca);
    }

    public void addCaseAttachments(List<CaseAttachment> ca) {
        if (caseAttachments == null) {
            caseAttachments = new ArrayList<>();
        }
        caseAttachments.addAll(ca);
    }
}
