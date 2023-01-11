package uk.gov.digital.ho.hocs.cms.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "reference")
public class Reference {

    @Id
    @Column(name="referenceid")
    private BigDecimal referenceid;

    @Column(name = "partyid", nullable = false, insertable = false, updatable = false)
    private BigDecimal partyid;

    @Column(name="reftype")
    private String refType;

    @Column
    private String reference;
}
