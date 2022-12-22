package uk.gov.digital.ho.hocs.cms.domain.cms;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class References {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @ManyToOne
    @JoinColumn(name="id", nullable=false)
    private Individual individual;

    @Column
    private String refType;

    @Column
    private String reference;
}
