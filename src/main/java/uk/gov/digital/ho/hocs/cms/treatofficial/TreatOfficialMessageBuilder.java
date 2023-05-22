package uk.gov.digital.ho.hocs.cms.treatofficial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseDetails;
import uk.gov.digital.ho.hocs.cms.domain.model.CorrespondentTreatOfficial;
import uk.gov.digital.ho.hocs.cms.domain.model.Individual;
import uk.gov.digital.ho.hocs.cms.domain.repository.IndividualRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.TreatOfficialCorrespondentsRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
public class TreatOfficialMessageBuilder {

    private final IndividualRepository individualRepository;

    private final TreatOfficialCorrespondentsRepository treatOfficialCorrespondentsRepository;

    public TreatOfficialMessageBuilder(IndividualRepository individualRepository, TreatOfficialCorrespondentsRepository treatOfficialCorrespondentsRepository) {
        this.individualRepository = individualRepository;
        this.treatOfficialCorrespondentsRepository = treatOfficialCorrespondentsRepository;
    }

    public CaseDetails buildMessage(BigDecimal caseId) {
        List<CorrespondentTreatOfficial> caseLink = treatOfficialCorrespondentsRepository.findByCaseId(caseId);

        Optional<Individual> primaryCorrespondent = getPrimaryCorrespondent(caseLink);
        List<Optional<Individual>> representativeCorrespondents = getRepresentativeCorrespondents(caseLink);

        Individual individual = null;
        individual = primaryCorrespondent.get();

        List<Individual> representatives = getRepresentatives(representativeCorrespondents);


        return null;
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

}
