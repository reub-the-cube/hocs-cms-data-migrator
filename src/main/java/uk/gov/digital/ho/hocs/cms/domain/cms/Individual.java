package uk.gov.digital.ho.hocs.cms.domain.cms;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name="individual")
public class Individual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column
    private String forename;

    @Column
    private String surname;

    @Column
    private LocalDate dateOfBirth;

    @Column
    private String nationality;

    @Column
    private String telephone;

    @Column
    private String email;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "individual")
    private List<References> references;

}
