package uk.gov.digital.ho.hocs.cms.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CaseAttachment {

    private String documentPath;
    private String displayName;
    private String documentType;

}
