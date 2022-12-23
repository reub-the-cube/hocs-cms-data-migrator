package uk.gov.digital.ho.hocs.cms.domain.model;

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
    @JoinColumn(name="individual_id", nullable=false)
    private Individual individual;

    @Column
    private String refType;

    @Column
    private String reference;
}
