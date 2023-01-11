package uk.gov.digital.ho.hocs.cms.correspondents;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CorrespondentDetails {

    private LocalDate dateOfBirth;
    private String nationality;

}
