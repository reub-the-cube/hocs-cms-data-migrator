package uk.gov.digital.ho.hocs.cms.treatofficial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseDetails;
import uk.gov.digital.ho.hocs.cms.domain.message.Correspondent;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseDataTreatOfficial;
import uk.gov.digital.ho.hocs.cms.domain.model.CorrespondentTreatOfficial;
import uk.gov.digital.ho.hocs.cms.domain.model.Individual;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseDataTreatOfficialsRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.IndividualRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.TreatOfficialCorrespondentsRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class TreatOfficialMessageBuilder {

    public static final String TO_CASETYPE = "TO";
    public static final String CASE_STATUS_CLOSED = "Closed";
    private final IndividualRepository individualRepository;
    private final TreatOfficialCorrespondentsRepository treatOfficialCorrespondentsRepository;
    private final CaseDataTreatOfficialsRepository caseDataTreatOfficialsRepository;

    public TreatOfficialMessageBuilder(IndividualRepository individualRepository,
                                       TreatOfficialCorrespondentsRepository treatOfficialCorrespondentsRepository,
                                       CaseDataTreatOfficialsRepository caseDataTreatOfficialsRepository) {
        this.individualRepository = individualRepository;
        this.treatOfficialCorrespondentsRepository = treatOfficialCorrespondentsRepository;
        this.caseDataTreatOfficialsRepository = caseDataTreatOfficialsRepository;
    }

    public CaseDetails buildMessage(BigDecimal caseId) {
        List<CorrespondentTreatOfficial> caseLink = treatOfficialCorrespondentsRepository.findByCaseId(caseId);

        Optional<Individual> primaryCorrespondent = getPrimaryCorrespondent(caseLink);
        List<Optional<Individual>> representativeCorrespondents = getRepresentativeCorrespondents(caseLink);

        Individual primary = null;
        primary = primaryCorrespondent.get();

        List<Individual> representatives = getRepresentatives(representativeCorrespondents);

        // populate correspondent part of message
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setPrimaryCorrespondent(extractedMigrationMessageCorrespondentDetails(primary));

        List<Correspondent> additionalCorrespondents = new ArrayList<>();
        for (Individual representative : representatives) {
            additionalCorrespondents.add(extractedMigrationMessageCorrespondentDetails(representative));
        }
        caseDetails.setAdditionalCorrespondents(additionalCorrespondents);

        // populate casedata
        CaseDataTreatOfficial caseDataTreatOfficial = caseDataTreatOfficialsRepository.findByCaseId(caseId);
        if (caseDataTreatOfficial == null) {
            throw new ApplicationExceptions.SendMigrationMessageException("No case data retrieved.", LogEvent.NO_CASE_DATA_TO_POPULATE_MESSAGE);
        }

        caseDetails.setCaseStatus(CASE_STATUS_CLOSED);
        caseDetails.setCreationDate(caseDataTreatOfficial.getOpenedDateTime());
        caseDetails.setCaseStatusDate(caseDataTreatOfficial.getClosedDateTime());
        caseDetails.setCaseType(TO_CASETYPE);

        return caseDetails;
    }

    public List<Individual> getRepresentatives(List<Optional<Individual>> representatives) {
        List<Individual> individuals = new ArrayList<>();
        for (Optional<Individual> rep : representatives) {
            if (rep.isPresent()) {
                individuals.add(rep.get());
            }
        }
        return individuals;
    }

    private Optional<Individual> getPrimaryCorrespondent(List<CorrespondentTreatOfficial> caseLink) {
        BigDecimal primaryId = null;
        for (CorrespondentTreatOfficial correspondent : caseLink) {
            if (correspondent.getIsPrimary()) {
                primaryId = correspondent.getCorrespondentId();
            }
       }
       return getIndividual(primaryId);
    }

    private List<Optional<Individual>> getRepresentativeCorrespondents(List<CorrespondentTreatOfficial> caseLink) {
        List<Optional<Individual>> representatives = new ArrayList<>();
        for (CorrespondentTreatOfficial correspondent : caseLink) {
            if (!correspondent.getIsPrimary()) {
                representatives.add(getIndividual(correspondent.getCorrespondentId()));
            }
        }
        return representatives;
    }

    private Optional<Individual> getIndividual(BigDecimal correspondentId) {
        Optional<Individual> individual = individualRepository.findById(correspondentId);
        if (!individual.isPresent()) {
            throw new ApplicationExceptions.SendMigrationMessageException(
                    String.format("Correspondent doesn't exist. Complainant ID {}", correspondentId),
                    LogEvent.MIGRATION_MESSAGE_FAILED);
        }
        return individualRepository.findById(correspondentId);
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
