package uk.gov.digital.ho.hocs.cms.correspondents;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.cms.domain.cms.Address;
import uk.gov.digital.ho.hocs.cms.domain.cms.Individual;
import uk.gov.digital.ho.hocs.cms.domain.cms.References;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.message.Correspondent;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent.*;

@Component
@Slf4j
public class CorrespondentExtractor {

    private static final String GET_CORRESPONDENT_IDS_FOR_CASE = "select complainantid, representativeid from FLODS_UKBACOMPLAINTS_D00 where caseid = :caseId";
    private static final String GET_CORRESPONDENT_NAME = "select forename1, surname from LGNOM_partyName where partyId = :partyId";
    private static final String GET_CORRESPONDENT_INDIVIDUAL_DETAILS = "select dateofbirth, nationality from LGNOM_individual where partyId = :partyId";
    private static final String GET_CORRESPONDENT_PHONE_NUMBER = "select phonenum from LGNOM_phoneDetails where partyId = :partyId";
    private static final String GET_CORRESPONDENT_EMAIL = "select emailaddress from LGNOM_emailDetails where partyId = :partyId";
    private static final String GET_CORRESPONDENT_ADDRESS = "select addressNum, addressLine1, addressLine2, addressLine3, addressLine4, addressLine5, addressLine6, postCode from LGNOM_partyAddress where partyId = :partyId";
    private static final String GET_CORRESPONDENT_REFERENCE = "select reftype, reference from LGNUK_REFERENCE where partyId = :partyId";

    private final DataSource dataSource;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public CorrespondentExtractor(@Qualifier("sqlServerDataSource") DataSource dataSource, @Qualifier("cms-template") NamedParameterJdbcTemplate namedParametersJdbcTemplate) {
        this.dataSource = dataSource;
        this.namedParameterJdbcTemplate = namedParametersJdbcTemplate;
    }

    public Correspondent getCorrespondentsForCase(BigDecimal caseId) {
        MapSqlParameterSource mapParameters = new MapSqlParameterSource();
        mapParameters.addValue("caseId", caseId);
        List<Map<String, Object>> rs = namedParameterJdbcTemplate.queryForList(GET_CORRESPONDENT_IDS_FOR_CASE, mapParameters);
        BigDecimal complainantId = null;
        BigDecimal representativeId = null;
        Individual primaryCorrespondent = null;
        Individual otherCorrespondent = null;
        if (rs.size() == 1) {
            Map result = rs.get(0);
            Object complainantidResult = result.get("complainantid");
            Object representativeidResult = result.get("representativeid");
            try {
                if (complainantidResult instanceof String s) {
                    complainantId = new BigDecimal(s);
                }
            } catch (NumberFormatException e) {
                log.error("Failed to convert ID for case {}", caseId);
                throw new ApplicationExceptions.ExtractCorrespondentException(
                        String.format("Failed to convert ID for case {}", caseId), COMPLAINANT_ID_INVALID, e);
                }

            try {
                if (representativeidResult instanceof String s) {
                    representativeId = new BigDecimal(s);
                }
            } catch (NumberFormatException e) {
                log.error("Failed to convert ID for case {}", caseId);
                throw new ApplicationExceptions.ExtractCorrespondentException(
                        String.format("Failed to convert ID for case {} ", caseId), REPRESENTATIVE_ID_INVALID, e);
            }
            log.debug("Case ID {} Complainant ID {} Representative ID {}", caseId, complainantId, representativeId);
        }

       if (complainantId.compareTo(representativeId) == 0){
           // complainant is primary correspondent
           try {
               primaryCorrespondent = getCorrespondentDetails(complainantId);
               primaryCorrespondent.setAddress(getAddress(complainantId));
               log.debug("Complainant ID {} data extracted. Case ID {}", complainantId, caseId);
           } catch (DataAccessException e) {
               log.error("Failed extracting correspondent details for complainant ID {} and case ID", complainantId, caseId);
               throw new ApplicationExceptions.ExtractCorrespondentException(
                       String.format("Failed extracting correspondent details for complainantID {} and case ID {}", complainantId, caseId),  CORRESPONDENT_EXTRACTION_FAILED, e);
           }
       } else {
           // representative is primary correspondent
           try {
               primaryCorrespondent = getCorrespondentDetails(representativeId);
               primaryCorrespondent.setAddress(getAddress(representativeId));
               otherCorrespondent = getCorrespondentDetails(complainantId);
               otherCorrespondent.setAddress(getAddress(complainantId));
               log.debug("Representative {} data extracted. Case ID {}", representativeId, caseId);
               log.debug("Complainant {} data extracted. Case ID {}", complainantId, caseId);
           } catch (DataAccessException e) {
               log.error("Failed extracting correspondent details for complainant ID {} representative ID {} and case ID", complainantId, representativeId, caseId);
               throw new ApplicationExceptions.ExtractCorrespondentException(
                       String.format("Failed extracting correspondent details for complainantID {}, representativeID {} and case ID {}", complainantId, representativeId, caseId),  CORRESPONDENT_EXTRACTION_FAILED, e);
           }

       }


       return null;
    }

    private Individual getCorrespondentDetails(BigDecimal partyId) {
        Individual individual = new Individual();
        individual.setPartyId(partyId);
        MapSqlParameterSource mapParameters = new MapSqlParameterSource();
        mapParameters.addValue("partyId", partyId);
        List<Map<String, Object>> rs = namedParameterJdbcTemplate.queryForList(GET_CORRESPONDENT_NAME, mapParameters);
        if (rs.size() == 1) {
            Map result = rs.get(0);
            Object forename = result.get("forename1");
            if (forename instanceof String s) {
                individual.setForename(s);
            }
            Object surname = result.get("surname");
            if (surname instanceof String s) {
                individual.setSurname(s);
            }
        }
        rs = namedParameterJdbcTemplate.queryForList(GET_CORRESPONDENT_INDIVIDUAL_DETAILS, mapParameters);
        if (rs.size() == 1) {
            Map result = rs.get(0);
            Object dateofbirth = result.get("dateofbirth");
            if (dateofbirth instanceof Timestamp ts) {
                individual.setDateOfBirth(ts.toLocalDateTime().toLocalDate());
            }
            Object nationality = result.get("nationality");
            if (nationality instanceof String s) {
                individual.setNationality(s);
            }
        }

        rs = namedParameterJdbcTemplate.queryForList(GET_CORRESPONDENT_PHONE_NUMBER, mapParameters);
        if (rs.size() == 1) {
            Map row = rs.get(0);
            Object telephone = row.get("phonenum");
            if (telephone instanceof String s) {
                individual.setTelephone(s);
            }
        }

        rs = namedParameterJdbcTemplate.queryForList(GET_CORRESPONDENT_EMAIL, mapParameters);
        if (rs.size() == 1) {
            Map result = rs.get(0);
            Object emailAddress = result.get("emailaddress");
            if (emailAddress instanceof String s) {
                individual.setEmail(s);
            }
        }

        rs = namedParameterJdbcTemplate.queryForList(GET_CORRESPONDENT_REFERENCE, mapParameters);
        List<References> references = new ArrayList<>();
        for(Map result: rs) {
            References reference = new References();
            Object refType = result.get("reftype");
            if (refType instanceof String s) {
                reference.setRefType(s);
            }
            Object referenceType = result.get("reference");
            if (referenceType instanceof String s) {
                reference.setReference(s);
            }
            references.add(reference);
        }
        return individual;
    }

    private Address getAddress(BigDecimal partyId) {
        Address address = new Address();
        MapSqlParameterSource mapParameters = new MapSqlParameterSource();
        mapParameters.addValue("partyId", partyId);
        List<Map<String, Object>> rs = namedParameterJdbcTemplate.queryForList(GET_CORRESPONDENT_ADDRESS, mapParameters);
        if (rs.size() == 1) {
            Map result = rs.get(0);
            Object addressNum = result.get("addressNum");
            if (addressNum instanceof String s) {
                address.setNumber(s);
            }
            Object addressLine1 = result.get("addressLine1");
            if (addressLine1 instanceof String s) {
                address.setAddressLine1(s);
            }
            Object addressLine2 = result.get("addressLine2");
            if (addressLine2 instanceof String s) {
                address.setAddressLine2(s);
            }
            Object addressLine3 = result.get("addressLine3");
            if (addressLine3 instanceof String s) {
                address.setAddressLine3(s);
            }
            Object addressLine4 = result.get("addressLine4");
            if (addressLine4 instanceof String s) {
                address.setAddressLine4(s);
            }
            Object addressLine5 = result.get("addressLine5");
            if (addressLine5 instanceof String s) {
                address.setAddressLine5(s);
            }
            Object addressLine6 = result.get("addressLine6");
            if (addressLine6 instanceof String s) {
                address.setAddressLine6(s);
            }
            Object postcode = result.get("postcode");
            if (postcode instanceof String s) {
                address.setPostcode(s);
            }
        }
        return address;
    }



}
