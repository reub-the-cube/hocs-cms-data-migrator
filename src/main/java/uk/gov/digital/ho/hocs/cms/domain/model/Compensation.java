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
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "compensation")
public class Compensation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name = "caseid")
    private BigDecimal caseId;

    @Column(name = "dateofcompensationclaim")
    private String dateOfCompensationClaim;

    @Column(name = "offeraccepted")
    private String offerAccepted;

    @Column(name = "dateofpayment")
    private String dateOfPayment;

    @Column(name = "compensationamount")
    private BigDecimal compensationAmmount;

    @Column(name = "amountclaimed")
    private BigDecimal amountClaimed;

    @Column(name = "amountoffered")
    private BigDecimal amountOffered;

    @Column(name = "consolatorypayment")
    private BigDecimal consolatoryPayment;
}
