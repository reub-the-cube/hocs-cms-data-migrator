package uk.gov.digital.ho.hocs.cms.document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Correspondent {

    private String fullName;
    private String correspondentType;
    private String address1;
    private String address2;
    private String address3;
    private String postcode;
    private String country;
    private String organisation;
    private String telephone;
    private String email;
    private String reference;
}
