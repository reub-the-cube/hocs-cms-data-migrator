package uk.gov.digital.ho.hocs.cms.domain.cms;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Setter
@Getter
public class Individual {

    private String forename;
    private String surname;
    private Timestamp dateOfBirth;
    private String nationality;
    private String telephone;
    private String email;
    private Address address;
    private List<Reference> references;

}
