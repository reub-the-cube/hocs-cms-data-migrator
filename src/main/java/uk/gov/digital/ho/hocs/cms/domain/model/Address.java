package uk.gov.digital.ho.hocs.cms.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Setter
@Getter
@Entity
@Table(name="address")
public class Address {

    @Id
    @Column(name="addressid")
    private BigDecimal addressId;

    @Column
    private String number;

    @Column(name = "addressline1")
    private String addressLine1;

    @Column(name = "addressline2")
    private String addressLine2;

    @Column(name = "addressline3")
    private String addressLine3;

    @Column(name = "addressline4")
    private String addressLine4;

    @Column(name = "addressline5")
    private String addressLine5;

    @Column(name = "addressline6")
    private String addressLine6;

    @Column
    private String postcode;
}
