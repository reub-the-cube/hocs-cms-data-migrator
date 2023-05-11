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
@Table(name = "case_data_treat_officials")
public class CaseDataTreatOfficial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name = "caseid")
    private BigDecimal caseId;

    @Column(name = "typeid")
    private String typeId;

    @Column(name = "lettertopic")
    private String letterTopic;

    @Column(name = "openeddatetime")
    private String openedDateTime;

    @Column(name = "allocatedtodeptid")
    private String allocatedToDeptId;

    @Column(name = "responsedate")
    private String responseDate;

    @Column(name = "tx_rejectnotes")
    private String txRejectNotes;

    @Column(name = "caseref")
    private String caseRef;

    @Column(name = "targetfixdatetime")
    private String targetFixDateTime;

    @Column(name = "otherdescription")
    private String otherDescription;

    @Column(name = "title")
    private String title;

    @Column(name = "closeddatetime")
    private String closedDateTime;

    @Column(name = "severity")
    private BigDecimal severity;

    @Column(name = "priority")
    private BigDecimal priority;

    @Column(name = "status")
    private BigDecimal status;

}