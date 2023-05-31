package uk.gov.digital.ho.hocs.cms.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseDataItem;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseDataComplaint;
import uk.gov.digital.ho.hocs.cms.domain.model.ComplaintCase;
import uk.gov.digital.ho.hocs.cms.domain.model.Individual;
import uk.gov.digital.ho.hocs.cms.domain.model.RiskAssessment;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseDataComplaintsRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.CasesRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.IndividualRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.RiskAssessmentRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class POGRMessageCaseData {

    private final IndividualRepository individualRepository;
    private final CasesRepository casesRepository;
    private final CaseDataComplaintsRepository caseDataComplaintsRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;


    public POGRMessageCaseData(IndividualRepository individualRepository, CasesRepository casesRepository,
                               CaseDataComplaintsRepository caseDataComplaintsRepository,
                               RiskAssessmentRepository riskAssessmentRepository) {
        this.individualRepository = individualRepository;
        this.casesRepository = casesRepository;
        this.caseDataComplaintsRepository = caseDataComplaintsRepository;
        this.riskAssessmentRepository = riskAssessmentRepository;
    }

    public List<CaseDataItem> extractCaseData(BigDecimal caseId) {
        ComplaintCase complaintCase = casesRepository.findByCaseId(caseId);
        Optional<Individual> individualOptional = individualRepository.findById(complaintCase.getComplainantId());
        Individual individual = null;
        if (individualOptional.isPresent()) {
            individual = individualOptional.get();
        } else {
            throw new ApplicationExceptions.SendMigrationMessageException(
                    String.format("Complainant doesn't exist. Complainant ID {}", complaintCase.getComplainantId()),
                    LogEvent.NO_COMPLAINANT_FOUND_FOR_CASE_DATA);
        }

        CaseDataComplaint caseDataComplaint = caseDataComplaintsRepository.findByCaseId(caseId);
        if (caseDataComplaint == null) {
            throw new ApplicationExceptions.SendMigrationMessageException("No case data retrieved.", LogEvent.NO_CASE_DATA_TO_POPULATE_MESSAGE);
        }

        RiskAssessment riskAssessment = riskAssessmentRepository.findByCaseId(caseId);

        List caseDataItems = new ArrayList();
        CaseDataItem caseDataItem = new CaseDataItem();
        caseDataItem.setName("ComplainantDOB");
        caseDataItem.setValue(individual.getDateOfBirth().toString());
        caseDataItems.add(caseDataItem);

        caseDataItem = new CaseDataItem();
        caseDataItem.setName("ComplaintNationOrigin");
        caseDataItem.setValue(individual.getNationality());
        caseDataItems.add(caseDataItem);

        caseDataItem = new CaseDataItem();
        caseDataItem.setName("ComplaintDescription");
        caseDataItem.setValue(caseDataComplaint.getDescription());
        caseDataItems.add(caseDataItem);

        caseDataItem = new CaseDataItem();
        caseDataItem.setName("BusinessArea");
        caseDataItem.setValue(caseDataComplaint.getBusinessArea());
        caseDataItems.add(caseDataItem);

        if (caseDataComplaint.getResponseDate() != null) {
            caseDataItem = new CaseDataItem();
            caseDataItem.setName("DispatchDate");
            caseDataItem.setValue(caseDataComplaint.getResponseDate());
            caseDataItems.add(caseDataItem);
        }

        caseDataItem = new CaseDataItem();
        caseDataItem.setName("ComplaintChannel");
        if (caseDataComplaint.getChannel() != null) {
            caseDataItem.setValue(caseDataComplaint.getChannel().toString());
        } else {
            caseDataItem.setValue("Unknown");
        }
        caseDataItems.add(caseDataItem);

        if (individual.getCompanyName() != null) {
            caseDataItem = new CaseDataItem();
            caseDataItem.setName("ComplainantCompanyName");
            caseDataItem.setValue(individual.getCompanyName());
            caseDataItems.add(caseDataItem);
        }

        return caseDataItems;
    }
}
