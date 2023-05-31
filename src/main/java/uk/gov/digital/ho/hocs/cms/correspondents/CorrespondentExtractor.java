package uk.gov.digital.ho.hocs.cms.correspondents;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.model.Address;
import uk.gov.digital.ho.hocs.cms.domain.model.ComplaintCase;
import uk.gov.digital.ho.hocs.cms.domain.model.Individual;
import uk.gov.digital.ho.hocs.cms.domain.model.Reference;
import uk.gov.digital.ho.hocs.cms.domain.repository.CasesRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.IndividualRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;

import static uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent.CORRESPONDENT_EXTRACTION_FAILED;

@Component
@Slf4j
public class CorrespondentExtractor {

    private static final String GET_CORRESPONDENT_IDS_FOR_CASE = "select complainantid, representativeid from FLODS_UKBACOMPLAINTS_D00 where caseid = ?";
    private static final String GET_CORRESPONDENT_NAME = "select top 1 forename1, surname from LGNOM_partyName where " +
            "  partyId = ? ORDER BY CASE currentName WHEN 1 THEN 1 ELSE 0  END desc, LastModifiedDate DESC, ID DESC";
    private static final String GET_CORRESPONDENT_INDIVIDUAL_DETAILS = "select dateofbirth, nationality from LGNOM_individual where partyId = ?";
    private static final String GET_CORRESPONDENT_PHONE_NUMBER = "select top 1 phonenum from LGNOM_phoneDetails where partyId = ? ORDER BY CASE preferred WHEN 1 THEN 1 ELSE 0  END desc, LastModifiedDate DESC, phoneId DESC";
    private static final String GET_CORRESPONDENT_EMAIL = "select top 1 emailaddress from LGNOM_emailDetails where partyId = ? ORDER BY CASE preferred WHEN 1 THEN 1 ELSE 0  END desc, LastModifiedDate DESC, emailId DESC";
    private static final String GET_CORRESPONDENT_ADDRESS = "select top 1 ID, addressNum, addressLine1, addressLine2, addressLine3, addressLine4, addressLine5, addressLine6, postCode from LGNOM_partyAddress where partyId = ?  " +
            "  ORDER BY CASE preferred WHEN 1 THEN 1 ELSE 0  END desc, LastModifiedDate DESC, ID DESC";
    private static final String GET_CORRESPONDENT_REFERENCE = "select ID, reftype, reference from LGNUK_REFERENCE where partyId = ?";

    private static final String GET_CORRESPONDENT_COMPANY_NAME = "select UserDefinedText1 as companyname from LGNOM_individual where partyId = ? ";
    private final DataSource dataSource;

    private final JdbcTemplate jdbcTemplate;

    private final IndividualRepository individualRepository;

    private final CasesRepository casesRepository;

    public CorrespondentExtractor(@Qualifier("cms") DataSource dataSource, IndividualRepository individualRepository,
                                  CasesRepository casesRepository) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.individualRepository = individualRepository;
        this.casesRepository = casesRepository;
    }

    @Transactional
    public void getCorrespondentsForCase(BigDecimal caseId) {

        Correspondents correspondents = jdbcTemplate.queryForObject(GET_CORRESPONDENT_IDS_FOR_CASE,
                (rs, rowNum) -> {
                    Correspondents c = new Correspondents();
                    c.setComplainantId(rs.getBigDecimal("complainantid"));
                    c.setRepresentativeId(rs.getBigDecimal("representativeid"));
                    return c;}, caseId);

        Individual primaryCorrespondent;
        Individual otherCorrespondent = null;
        ComplaintCase complaintCase = new ComplaintCase();
        log.debug("Case ID {} Complainant ID {} Representative ID {}", caseId, correspondents.getComplainantId(),
                       correspondents.getRepresentativeId());

        if (correspondents.isComplainantIdNull()) {
            throw new ApplicationExceptions.ExtractCorrespondentException(
                    String.format("Failed extracting correspondent IDs for case ID %s", caseId),  CORRESPONDENT_EXTRACTION_FAILED);
        }

        // if there is no reprentative id make it equal to complainant id
        if (correspondents.isRepresentativeIdNull()) {
            correspondents.setRepresentativeId(correspondents.getComplainantId());
        }

        if (correspondents.isComplainantPrimaryCorrespondent()) {
            try {
                primaryCorrespondent = getCorrespondentDetails(correspondents.getComplainantId());
                primaryCorrespondent.setAddress(getAddress(correspondents.getComplainantId()));
                primaryCorrespondent.setPrimary(true);
                primaryCorrespondent.setType(CorrespondentType.COMPLAINANT.toString());
                complaintCase.setCaseId(caseId);
                complaintCase.setComplainantId(correspondents.getComplainantId());
                complaintCase.setRepresentativeId(correspondents.getComplainantId());
                log.debug("Complainant ID {} data extracted. Case ID {}", correspondents.getComplainantId(), caseId);
                individualRepository.save(primaryCorrespondent);
                casesRepository.deleteAllByCaseId(caseId);
                casesRepository.save(complaintCase);
            } catch (DataAccessException e) {
                log.error("Failed extracting correspondent details for complainant ID {} and case ID {}", correspondents.getComplainantId(), caseId);
                throw new ApplicationExceptions.ExtractCorrespondentException(
                        e.getMessage(), CORRESPONDENT_EXTRACTION_FAILED, e);
            }
        } else {
            // representative is primary correspondent
            try {
                primaryCorrespondent = getCorrespondentDetails(correspondents.getRepresentativeId());
                primaryCorrespondent.setAddress(getAddress(correspondents.getRepresentativeId()));
                primaryCorrespondent.setType(CorrespondentType.THIRD_PARTY_REP.toString());
                primaryCorrespondent.setPrimary(true);
                otherCorrespondent = getCorrespondentDetails(correspondents.getComplainantId());
                otherCorrespondent.setAddress(getAddress(correspondents.getComplainantId()));
                otherCorrespondent.setType(CorrespondentType.COMPLAINANT.toString());
                otherCorrespondent.setPrimary(false);
                complaintCase.setCaseId(caseId);
                complaintCase.setComplainantId(correspondents.getComplainantId());
                complaintCase.setRepresentativeId(correspondents.getRepresentativeId());

                log.debug("Representative {} data extracted. Case ID {}", correspondents.getRepresentativeId(), caseId);
                log.debug("Complainant {} data extracted. Case ID {}", correspondents.getComplainantId(), caseId);
                individualRepository.save(primaryCorrespondent);
                individualRepository.save(otherCorrespondent);
                casesRepository.deleteAllByCaseId(caseId);
                casesRepository.save(complaintCase);
            } catch (DataAccessException e) {
                log.error("Failed extracting correspondent details for complainant ID {} representative ID {} and case ID", correspondents.getComplainantId(), correspondents.getRepresentativeId(), caseId);
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

        CorrespondentCompanyName correspondentCompanyName = jdbcTemplate.queryForObject(GET_CORRESPONDENT_COMPANY_NAME, (rs, rowNum) -> {
            CorrespondentCompanyName cn = new CorrespondentCompanyName();
            cn.setCompanyName(rs.getString("companyname"));
            return cn;
        }, partyId);
        individual.setCompanyName(correspondentCompanyName.getCompanyName());

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
