package uk.gov.digital.ho.hocs.cms.treatofficial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.cms.correspondents.CorrespondentDetails;
import uk.gov.digital.ho.hocs.cms.correspondents.CorrespondentEmail;
import uk.gov.digital.ho.hocs.cms.correspondents.CorrespondentName;
import uk.gov.digital.ho.hocs.cms.correspondents.CorrespondentPhoneNumber;
import uk.gov.digital.ho.hocs.cms.correspondents.CorrespondentType;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.model.Address;
import uk.gov.digital.ho.hocs.cms.domain.model.Individual;
import uk.gov.digital.ho.hocs.cms.domain.model.Reference;
import uk.gov.digital.ho.hocs.cms.domain.repository.IndividualRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;

import java.util.List;

import static uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent.CORRESPONDENT_EXTRACTION_FAILED;

@Component
@Slf4j
public class TreatOfficialCorrespondentExtractor {

    private static final String GET_CORRESPONDENT_IDS_FOR_CASE = "select xref1 as complainantId from LGNCC_CLOSEDCASEVIEW where CaseId = ?";
    private static final String GET_THIRD_PARTY_CORRESPONDENT_IDS_FOR_CASE = "select ClientID as representativeId from LGNCC_INTLOGHDR where logid in (select InteractionID from LGNCC_ENQUIRYRELATION where CaseID = ?)";

    // RICHARDS
    private static final String GET_CORRESPONDENT_NAME = "select forename1, surname from LGNOM_partyName where partyId = ?";
    private static final String GET_CORRESPONDENT_INDIVIDUAL_DETAILS = "select dateofbirth, nationality from LGNOM_individual where partyId = ?";
    private static final String GET_CORRESPONDENT_PHONE_NUMBER = "select phonenum from LGNOM_phoneDetails where partyId = ?";
    private static final String GET_CORRESPONDENT_EMAIL = "select emailaddress from LGNOM_emailDetails where partyId = ?";
    private static final String GET_CORRESPONDENT_ADDRESS = "select ID, addressNum, addressLine1, addressLine2, addressLine3, addressLine4, addressLine5, addressLine6, postCode from LGNOM_partyAddress where partyId = ?";
    private static final String GET_CORRESPONDENT_REFERENCE = "select ID, reftype, reference from LGNUK_REFERENCE where partyId = ?";

    private final DataSource dataSource;

    private final JdbcTemplate jdbcTemplate;

    private final IndividualRepository individualRepository;

    public TreatOfficialCorrespondentExtractor(@Qualifier("cms") DataSource dataSource, IndividualRepository individualRepository) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.individualRepository = individualRepository;
    }

    public void getCorrespondentsForCase(BigDecimal caseId) {

        BigDecimal correspondentId = jdbcTemplate.queryForObject(GET_CORRESPONDENT_IDS_FOR_CASE,
                (rs, rowNum) -> {
                    String c = new String();
                    c.equals(rs.getBigDecimal("complainantid"));
                    return new BigDecimal(c);
                }, caseId);

        List<BigDecimal> thirdPartyCorrespondentIds = jdbcTemplate.query(GET_THIRD_PARTY_CORRESPONDENT_IDS_FOR_CASE,
                (rs, rowNum) -> {
                    String c = new String();
                    c.equals(rs.getBigDecimal("representativeId"));
                    return new BigDecimal(c);
                }, caseId);

        extractPrimaryCorrespondent(caseId, correspondentId);

        extractThirdPartyCorrespondents(caseId, thirdPartyCorrespondentIds);
    }

    private void extractPrimaryCorrespondent(BigDecimal caseId, BigDecimal correspondentId) {
        try {
            Individual individual;
            individual = getCorrespondentDetails(correspondentId);
            individual.setAddress(getAddress(correspondentId));
            individual.setPrimary(true);
            individual.setType(CorrespondentType.COMPLAINANT.toString());
            log.debug("Complainant ID {} data extracted. Case ID {}", correspondentId, caseId);
            individualRepository.save(individual);
        } catch (DataAccessException e) {
            log.error("Failed extracting correspondent details for complainant ID {} and case ID", correspondentId, caseId);
            throw new ApplicationExceptions.ExtractCorrespondentException(
                    e.getMessage(), CORRESPONDENT_EXTRACTION_FAILED, e);
        }
    }

    private void extractThirdPartyCorrespondents(BigDecimal caseId, List<BigDecimal> correspondentIds) {
        for (BigDecimal correspondentId : correspondentIds) {
            try {
                Individual individual;
                individual = getCorrespondentDetails(correspondentId);
                individual.setAddress(getAddress(correspondentId));
                individual.setType(CorrespondentType.THIRD_PARTY_REP.toString());
                individual.setPrimary(false);
                log.debug("Representative {} data extracted. Case ID {}", correspondentIds, caseId);
                individualRepository.save(individual);
            } catch (DataAccessException e) {
                log.error("Failed extracting correspondent details for representative ID {} and case ID", correspondentId, caseId);
                throw new ApplicationExceptions.ExtractCorrespondentException(e.getMessage(),  CORRESPONDENT_EXTRACTION_FAILED, e);
            }
        }
    }

    private Individual getCorrespondentDetails(BigDecimal partyId) {
        Individual individual = new Individual();
        individual.setPartyId(partyId);
        CorrespondentName name = jdbcTemplate.queryForObject(GET_CORRESPONDENT_NAME, (rs, rowNum) -> {
            CorrespondentName cn = new CorrespondentName();
            cn.setForename(rs.getString ("forename1"));
            cn.setSurname(rs.getString("surname"));
            return cn;
        }, partyId);
        individual.setForename(name.getForename());
        individual.setSurname(name.getSurname());

        CorrespondentDetails details = jdbcTemplate.queryForObject(GET_CORRESPONDENT_INDIVIDUAL_DETAILS, (rs, rowNum) -> {
            CorrespondentDetails cd = new CorrespondentDetails();
            cd.setDateOfBirth(rs.getTimestamp("dateofbirth").toLocalDateTime().toLocalDate());
            cd.setNationality(rs.getString("nationality"));
            return cd;
        }, partyId);
        individual.setDateOfBirth(details.getDateOfBirth());
        individual.setNationality(details.getNationality());

        CorrespondentPhoneNumber phone = jdbcTemplate.queryForObject(GET_CORRESPONDENT_PHONE_NUMBER, (rs, rowNum) -> {
            CorrespondentPhoneNumber cpn = new CorrespondentPhoneNumber();
            cpn.setPhoneNumber(rs.getString("phonenum"));
            return cpn;
        }, partyId);
        individual.setTelephone(phone.getPhoneNumber());

        CorrespondentEmail email = jdbcTemplate.queryForObject(GET_CORRESPONDENT_EMAIL, (rs, rowNum) -> {
            CorrespondentEmail ce = new CorrespondentEmail();
            ce.setEmail(rs.getString("emailaddress"));
            return ce;
        }, partyId);
        individual.setEmail(email.getEmail());

        individual.setReferences(jdbcTemplate.query(GET_CORRESPONDENT_REFERENCE, (rs, rowNum) -> {
            Reference r = new Reference();
            r.setReferenceid(rs.getBigDecimal("ID"));
            r.setRefType(rs.getString("reftype"));
            r.setReference(rs.getString("reference"));
            return r;
        }, partyId));

        return individual;
    }

    private Address getAddress(BigDecimal partyId) {
        Address address = jdbcTemplate.queryForObject(GET_CORRESPONDENT_ADDRESS, (rs, rowNum) -> {
            Address a = new Address();
            a.setAddressId(rs.getBigDecimal("ID"));
            a.setNumber(rs.getString("addressNum"));
            a.setAddressLine1(rs.getString("addressLine1"));
            a.setAddressLine2(rs.getString("addressLine2"));
            a.setAddressLine3(rs.getString("addressLine3"));
            a.setAddressLine4(rs.getString("addressLine4"));
            a.setAddressLine5(rs.getString("addressLine5"));
            a.setAddressLine6(rs.getString("addressLine6"));
            a.setPostcode(rs.getString("postcode"));
            return a;
        }, partyId);

        return address;
    }
}