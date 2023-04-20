package uk.gov.digital.ho.hocs.cms.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseDataItem;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseData;
import uk.gov.digital.ho.hocs.cms.domain.model.Categories;
import uk.gov.digital.ho.hocs.cms.domain.model.ComplaintCase;
import uk.gov.digital.ho.hocs.cms.domain.model.Individual;
import uk.gov.digital.ho.hocs.cms.domain.model.Reference;
import uk.gov.digital.ho.hocs.cms.domain.model.RiskAssessment;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseHistoryRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.CasesRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.CategoriesRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.IndividualRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.RiskAssessmentRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ComplaintsMessageCaseData {
    private final CaseHistoryRepository caseHistoryRepository;

    private final IndividualRepository individualRepository;
    private final CaseDataRepository caseDataRepository;
    private final CategoriesRepository categoriesRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final CasesRepository casesRepository;
    private final CaseDataTypes caseDataTypes;

    public ComplaintsMessageCaseData(IndividualRepository individualRepository,
                                     CaseDataRepository caseDataRepository,
                                     CategoriesRepository categoriesRepository,
                                     RiskAssessmentRepository riskAssessmentRepository,
                                     CaseHistoryRepository caseHistoryRepository,
                                     CasesRepository casesRepository,
                                     CaseDataTypes caseDataTypes) {
        this.individualRepository = individualRepository;
        this.caseDataRepository = caseDataRepository;
        this.categoriesRepository = categoriesRepository;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.caseHistoryRepository = caseHistoryRepository;
        this.casesRepository = casesRepository;
        this.caseDataTypes = caseDataTypes;
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
        CaseData caseData = caseDataRepository.findByCaseId(caseId);
        if (caseData == null) {
            throw new ApplicationExceptions.SendMigrationMessageException("No case data retrieved.", LogEvent.NO_CASE_DATA_TO_POPULATE_MESSAGE);
        }

        RiskAssessment riskAssessment = riskAssessmentRepository.findByCaseId(caseId);
        // populate case data hocs-6149
        List caseDataItems = new ArrayList();
        CaseDataItem caseDataItem = new CaseDataItem();
        caseDataItem.setName("deadline");
        caseDataItem.setValue(caseData.getSlaDate());
        caseDataItem = new CaseDataItem();
        caseDataItem.setName("DateReceived");
        caseDataItem.setValue(caseData.getReceiveDate());
        caseDataItem = new CaseDataItem();
        caseDataItem.setName("ComplainantDOB");
        caseDataItem.setValue(individual.getDateOfBirth().toString());
        caseDataItems.add(caseDataItem);
        caseDataItem = new CaseDataItem();
        caseDataItem.setName("ComplainantNationality");
        caseDataItem.setValue(individual.getNationality());
        caseDataItems.add(caseDataItem);
        caseDataItem = new CaseDataItem();
        caseDataItem.setName("ComplainantHORef");
        caseDataItem.setValue(extractRef(individual.getReferences(), "Home Office Ref"));
        caseDataItems.add(caseDataItem);
        caseDataItem = new CaseDataItem();
        caseDataItem.setName("ComplainantPortRef");
        caseDataItem.setValue(extractRef(individual.getReferences(), "Port Reference"));
        caseDataItems.add(caseDataItem);
        caseDataItem = new CaseDataItem();
        caseDataItem.setName("CompType");
        caseDataItem.setValue(caseData.getCurrentType());
        caseDataItems.add(caseDataItem);
        caseDataItem = new CaseDataItem();
        caseDataItem.setName("CaseSummary");
        caseDataItem.setValue(caseData.getDescription());
        caseDataItems.add(caseDataItem);
        // Categories
        caseDataItems.addAll(extractCategories(caseId));
        caseDataItem = new CaseDataItem();
        caseDataItem.setName("OwningCSU");
        caseDataItem.setValue(caseData.getOwningCsu());
        caseDataItems.add(caseDataItem);
        caseDataItem = new CaseDataItem();
        caseDataItem.setName("CchCompType");
        caseDataItem.setValue(caseData.getCurrentType());
        caseDataItems.add(caseDataItem);
        caseDataItem = new CaseDataItem();
        caseDataItem.setName("SeveritySafeGuarding");
        caseDataItem.setValue(riskAssessment.getFromOrAffectingAChild());
        caseDataItems.add(caseDataItem);
        caseDataItem = new CaseDataItem();
        caseDataItem.setName("BusArea");
        caseDataItem.setValue(caseData.getBusinessArea());
        caseDataItems.add(caseDataItem);
        caseDataItem = new CaseDataItem();
        caseDataItem.setName("DateResponded");
        caseDataItem.setValue(caseData.getClosedDt());
        caseDataItems.add(caseDataItem);


        return caseDataItems;
    }

    private String extractRef(List<Reference> references, String refType) {
        List<String> ref = references.stream().filter(type -> type.getRefType().equalsIgnoreCase(refType))
                .map(r -> r.getReference()).collect(Collectors.toList());
        if (ref.size() == 1) {
            return ref.get(0);
        } else {
            return "";
        }
    }

    private List<CaseDataItem> extractCategories(BigDecimal caseId) {
        List<CaseDataItem> caseDataItems = new ArrayList<>();
        List<Categories> categories = categoriesRepository.findAllByCaseId(caseId);
        Map<String, String> checkedCategories = mapCheckedCategories(categories);
        for (Map.Entry<String, String> entry : caseDataTypes.getCategories().entrySet()) {
            if (checkedCategories.containsKey(entry.getKey())) {
                caseDataItems.add(makeCaseDataItem(entry.getValue(), Boolean.TRUE.toString()));
            } else {
                caseDataItems.add(makeCaseDataItem(entry.getValue(), Boolean.FALSE.toString()));
            }
        }
        return caseDataItems;
    }

    private Map<String, String> mapCheckedCategories(List<Categories> categories) {
        Map<String, String> checkedCategories = new HashMap<>();
        for (Categories category : categories) {
            checkedCategories.put(category.getCategory(), category.getSelected());
        }
        return checkedCategories;
    }

    private CaseDataItem makeCaseDataItem(String name, String checked) {
        CaseDataItem caseDataItem = new CaseDataItem();
        caseDataItem.setName(name);
        caseDataItem.setValue(checked);
        return caseDataItem;
    }



}
