package uk.gov.digital.ho.hocs.cms.domain.cms;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
public class Individual {

    private String forename;
    private String surname;
    private LocalDate dateOfBirth;
    private String nationality;
    private String telephone;
    private String email;
    private Address address;
    private List<References> references;

}
