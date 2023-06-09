package uk.gov.digital.ho.hocs.cms.treatofficial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseDataItem;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseDataTreatOfficial;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseDataTreatOfficialsRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TreatOfficialMessageCaseData {

    private final CaseDataTreatOfficialsRepository caseDataTreatOfficialsRepository;

    public TreatOfficialMessageCaseData(CaseDataTreatOfficialsRepository caseDataTreatOfficialsRepository) {
        this.caseDataTreatOfficialsRepository = caseDataTreatOfficialsRepository;
    }

    public List<CaseDataItem> extractCaseData(BigDecimal caseId) {

        CaseDataTreatOfficial caseDataTreatOfficial = caseDataTreatOfficialsRepository.findByCaseId(caseId);
        if (caseDataTreatOfficial == null) {
            throw new ApplicationExceptions.SendMigrationMessageException("No case data retrieved.", LogEvent.NO_CASE_DATA_TO_POPULATE_MESSAGE);
        }

        List<CaseDataItem> caseDataItems = new ArrayList<>();
        CaseDataItem caseDataItem = new CaseDataItem();

        if (caseDataTreatOfficial.getLetterTopic() != null) {
            caseDataItem.setName("LetterTopic");
            caseDataItem.setValue(caseDataTreatOfficial.getLetterTopic());
            caseDataItems.add(caseDataItem);
        }

        caseDataItem = new CaseDataItem();
        caseDataItem.setName("OpenedDateTime");
        caseDataItem.setValue(caseDataTreatOfficial.getOpenedDateTime());
        caseDataItems.add(caseDataItem);

        caseDataItem = new CaseDataItem();
        caseDataItem.setName("TypeID");
        caseDataItem.setValue(caseDataTreatOfficial.getTypeId());
        caseDataItems.add(caseDataItem);

        if (caseDataTreatOfficial.getAllocatedToDeptId() != null) {
            caseDataItem = new CaseDataItem();
            caseDataItem.setName("AllocatedToDeptID");
            caseDataItem.setValue(caseDataTreatOfficial.getAllocatedToDeptId());
            caseDataItems.add(caseDataItem);
        }

        if (caseDataTreatOfficial.getResponseDate() != null) {
            caseDataItem = new CaseDataItem();
            caseDataItem.setName("ResponseDate");
            caseDataItem.setValue(caseDataTreatOfficial.getResponseDate());
            caseDataItems.add(caseDataItem);
        }

        if (caseDataTreatOfficial.getTxRejectNotes() != null) {
            caseDataItem = new CaseDataItem();
            caseDataItem.setName("tx_rejectnotes");
            caseDataItem.setValue(caseDataTreatOfficial.getTxRejectNotes());
            caseDataItems.add(caseDataItem);
        }

        caseDataItem = new CaseDataItem();
        caseDataItem.setName("TargetDate");
        caseDataItem.setValue(caseDataTreatOfficial.getTargetDate());
        caseDataItems.add(caseDataItem);

        return caseDataItems;
    }

}