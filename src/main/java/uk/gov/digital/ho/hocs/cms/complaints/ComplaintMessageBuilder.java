package uk.gov.digital.ho.hocs.cms.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.cms.casedata.CaseTypeMapping;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseDetails;
import uk.gov.digital.ho.hocs.cms.domain.message.Correspondent;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseDataComplaint;
import uk.gov.digital.ho.hocs.cms.domain.model.ComplaintCase;
import uk.gov.digital.ho.hocs.cms.domain.model.Individual;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseDataComplaintRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.CasesRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.IndividualRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ComplaintMessageBuilder {

    private final IndividualRepository individualRepository;
    private final CaseDataComplaintRepository caseDataComplaintRepository;
    private final CasesRepository casesRepository;

    public ComplaintMessageBuilder(IndividualRepository individualRepository,
                                   CaseDataComplaintRepository caseDataComplaintRepository,
                                   CasesRepository casesRepository) {
        this.individualRepository = individualRepository;
        this.caseDataComplaintRepository = caseDataComplaintRepository;
        this.casesRepository = casesRepository;
    }

    public CaseDetails buildMessage(BigDecimal caseId) {
        ComplaintCase complaintCase = casesRepository.findByCaseId(caseId);
        Optional<Individual> individualOpt = individualRepository.findById(complaintCase.getComplainantId());
        Optional<Individual> representativeOpt = individualRepository.findById(complaintCase.getRepresentativeId());

        Individual individual = null;
        Individual representative = null;
        if (individualOpt.isPresent()) {
            individual = individualOpt.get();
        } else {
            throw new ApplicationExceptions.SendMigrationMessageException(
                    String.format("Complainant doesn't exist. Complainant ID {}", complaintCase.getComplainantId()),
                    LogEvent.MIGRATION_MESSAGE_FAILED);
        }

        if (representativeOpt.isPresent()) {
            representative = representativeOpt.get();
        }


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

        CaseDataComplaint caseDataComplaint = caseDataComplaintRepository.findByCaseId(caseId);
        if (caseDataComplaint == null) {
            throw new ApplicationExceptions.SendMigrationMessageException("No case data retrieved.", LogEvent.NO_CASE_DATA_TO_POPULATE_MESSAGE);
        }
        caseDetails.setCaseStatus(caseDataComplaint.getStatus());
        caseDetails.setCreationDate(caseDataComplaint.getReceiveDate());
        caseDetails.setCaseStatusDate(caseDataComplaint.getReceiveDate());
        caseDetails.setCaseType(CaseTypeMapping.getCaseType(caseDataComplaint.getOwningCsu()));
        if (caseDetails.getCaseType() == null) {
            throw new ApplicationExceptions.SendMigrationMessageException("NULL or UNKNOWN Case Types are ignored",
                    LogEvent.CASE_DATA_CASE_TYPE_IGNORED);
        }
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
