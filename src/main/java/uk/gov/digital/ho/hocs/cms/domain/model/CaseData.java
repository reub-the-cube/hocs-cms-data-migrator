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
@Table(name = "case_data")
public class CaseData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name = "caseid")
    private BigDecimal caseId;

    @Column(name = "casereference")
    private String caseReference;

    @Column(name = "ukbareceivedate")
    private String receiveDate;

    @Column(name = "casesladate")
    private String slaDate;

    @Column(name = "initialtype")
    private String initialType;

    @Column(name = "currenttype")
    private String currentType;

    @Column(name = "queuename")
    private String queueName;

    @Column(name = "location")
    private String location;

    @Column(name = "nrocombo")
    private String nroCombo;

    @Column(name = "closedt")
    private String closedDt;

    @Column(name = "owningcsu")
    private String owningCsu;

    @Column(name = "businessarea")
    private String businessArea;

    @Column(name = "status")
    private String status;

    @Column(name = "description")
    private String description;
}
