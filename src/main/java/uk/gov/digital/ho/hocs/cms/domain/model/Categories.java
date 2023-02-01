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
@Table(name = "categories")
public class Categories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name = "case_id")
    private BigDecimal caseId;

    @Column(name = "category")
    private String category;

    @Column(name = "selected")
    private String selected;

    @Column(name = "substantiated")
    private String substantiated;

    @Column(name = "amount")
    private BigDecimal amount;

}
