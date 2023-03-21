package uk.gov.digital.ho.hocs.cms.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.cms.correspondents.CorrespondentType;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseDetails;
import uk.gov.digital.ho.hocs.cms.domain.message.Correspondent;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseData;
import uk.gov.digital.ho.hocs.cms.domain.model.Individual;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.IndividualRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class ComplaintMessageBuilder {

    private final IndividualRepository individualRepository;
    private final CaseDataRepository caseDataRepository;

    public ComplaintMessageBuilder(IndividualRepository individualRepository,
                                   CaseDataRepository caseDataRepository) {
        this.individualRepository = individualRepository;
        this.caseDataRepository = caseDataRepository;
    }

    public CaseDetails buildMessage(BigDecimal caseId) {
        Individual individual = individualRepository.findIndividualComplainantByCaseId(caseId, CorrespondentType.COMPLAINANT.toString());
        Individual representative = individualRepository.findIndividualComplainantByCaseId(caseId, CorrespondentType.THIRD_PARTY_REP.toString());

        // populate correspondent part of message
        CaseDetails caseDetails = new CaseDetails();

        if (individual.getPrimary()) {
            caseDetails.setPrimaryCorrespondent(extractedMigrationMessageCorrespondentDetails(individual));
            caseDetails.setAdditionalCorrespondents(Collections.emptyList());
        } else {
            caseDetails.setPrimaryCorrespondent(extractedMigrationMessageCorrespondentDetails(representative));
            List<Correspondent> additionalCorrespondents = new ArrayList<>();
            additionalCorrespondents.add(extractedMigrationMessageCorrespondentDetails(individual));
            caseDetails.setAdditionalCorrespondents(additionalCorrespondents);
            }

        CaseData caseData = caseDataRepository.findByCaseId(caseId);
        if (caseData == null) caseData = new CaseData();
        caseDetails.setCaseStatus(caseData.getStatus());
        caseDetails.setCreationDate(caseData.getReceiveDate());
        caseDetails.setCaseStatusDate(caseData.getReceiveDate());
        caseDetails.setCaseType(caseData.getOwningCsu());
        return caseDetails;
    }

    private Correspondent extractedMigrationMessageCorrespondentDetails(Individual individual) {
        Correspondent correspondent = new Correspondent();
        correspondent.setFullName(String.format("%s %s", individual.getForename(), individual.getSurname()));
        correspondent.setEmail(individual.getEmail());
        correspondent.setCorrespondentType(individual.getType());
        correspondent.setAddress1(individual.getAddress().getAddressLine1());
        correspondent.setAddress2(individual.getAddress().getAddressLine2());
        correspondent.setAddress3(individual.getAddress().getAddressLine3());
        correspondent.setPostcode(individual.getAddress().getPostcode());
        correspondent.setTelephone(individual.getTelephone());
        return correspondent;
    }
}
