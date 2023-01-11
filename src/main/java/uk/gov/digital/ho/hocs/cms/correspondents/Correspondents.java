package uk.gov.digital.ho.hocs.cms.correspondents;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Correspondents {

    private BigDecimal complainantId;
    private BigDecimal representativeId;

    public boolean isComplainantPrimaryCorrespondent() {
        return complainantId.equals(representativeId);
    }

    public boolean isComplainantIdNull() {
        return complainantId == null;
    }

    public boolean isRepresentativeIdNull() {
        return representativeId == null;
    }
}
