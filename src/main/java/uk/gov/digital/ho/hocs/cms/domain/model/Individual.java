package uk.gov.digital.ho.hocs.cms.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name="individual")
public class Individual {

    @Id
    @Column(name = "partyid")
    private BigDecimal partyId;

    @Column
    private String forename;

    @Column
    private String surname;

    @Column(name = "dateofbirth")
    private LocalDate dateOfBirth;

    @Column
    private String nationality;

    @Column
    private String telephone;

    @Column
    private String email;

    @Column(name = "primarycorrespondent")
    private Boolean primary;

    @Column()
    private String type;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "addressid", referencedColumnName = "addressid")
    private Address address;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "partyid", referencedColumnName = "partyid", nullable = false)
    private List<Reference> references;

    @Column(name = "companyname")
    private String companyName;
}
