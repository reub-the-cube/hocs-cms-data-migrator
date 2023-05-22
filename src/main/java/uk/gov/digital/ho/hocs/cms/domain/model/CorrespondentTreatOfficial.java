package uk.gov.digital.ho.hocs.cms.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "correspondents_treat_officials")
public class CorrespondentTreatOfficial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name = "caseid")
    private BigDecimal caseId;

    @Column(name = "correspondentid")
    private BigDecimal correspondentId;

    @Column(name = "isprimary")
    private Boolean isPrimary;

}