package uk.gov.digital.ho.hocs.cms.domain.cms;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Table(name="address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @OneToOne(mappedBy = "address")
    private Individual individual;

    @Column
    private String name;

    @Column
    private String number;

    @Column
    private String addressLine1;

    @Column
    private String addressLine2;

    @Column
    private String addressLine3;

    @Column
    private String addressLine4;

    @Column
    private String addressLine5;

    @Column
    private String addressLine6;

    @Column
    private String postcode;


}
