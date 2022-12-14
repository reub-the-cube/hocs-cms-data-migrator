package uk.gov.digital.ho.hocs.cms.correspondents;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.cms.domain.cms.Address;
import uk.gov.digital.ho.hocs.cms.domain.cms.Individual;
import uk.gov.digital.ho.hocs.cms.domain.cms.Reference;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent.COMPLAINANT_ID_INVALID;
import static uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent.REPRESENTATIV_ID_INVALID;

@Component
@Slf4j
public class CorrespondentExtractor {

    private static final String GET_CORRESPONDENT_IDS_FOR_CASE = "select complainantid, representativeid from FLODS_UKBACOMPLAINTS_D00 where caseid = :caseId";
    private static final String GET_CORRESPONDENT_NAME = "select forename1, surname from LGNOM_partyName where partyId = :partyId";
    private static final String GET_CORRESPONDENT_INDIVIDUAL_DETAILS = "select dateofbirth, nationality from LGNOM_individual where partyId = :partyId";
    private static final String GET_CORRESPONDENT_PHONENUMBER = "select phonenum from LGNOM_phoneDetails where partyId = :partyId";
    private static final String GET_CORRESPONDENT_EMAIL = "select emailaddress from LGNOM_emailDetails where partyId = :partyId";
    private static final String GET_CORRESPONDENT_ADDRESS = "select addressNum, addressLine1, addressLine2, addressLine3, addressLine4, addressLine5, addressLine6, postCode from LGNOM_partyAddress where partyId = :partyId";
    private static final String GET_CORRESPONDENT_REFERENCE = "select reftype, reference from LGNUK_REFERENCE where partyId = :partyId";

    private final DataSource dataSource;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public CorrespondentExtractor(@Qualifier("cms") DataSource dataSource, @Qualifier("cms-template") NamedParameterJdbcTemplate namedParametersJdbcTemplate) {
        this.dataSource = dataSource;
        this.namedParameterJdbcTemplate = namedParametersJdbcTemplate;
    }

    public void getCorrespondentsForCase(BigDecimal caseId) {
        MapSqlParameterSource mapParameters = new MapSqlParameterSource();
        mapParameters.addValue("caseId", caseId);
        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(GET_CORRESPONDENT_IDS_FOR_CASE, mapParameters);
        BigDecimal complainantId = null;
        BigDecimal representativeId = null;
        Individual individual = null;
        //TODO: Check rows == 1
        for (Map row : rows) {
            Object complainantidResult = row.get("complainantid");
            Object representativeidResult = row.get("representativeid");
            try {
                if (complainantidResult instanceof String s) {
                    complainantId = new BigDecimal(s);
                }
            } catch (NumberFormatException e) {
                    throw new ApplicationExceptions.ExtractCorrespondentException(
                            String.format("Failed to convert ID for complainant: " + caseId), COMPLAINANT_ID_INVALID);
                }
            try {
                if (representativeidResult instanceof String s) {
                    representativeId = new BigDecimal(s);
                }

            } catch (NumberFormatException e) {
                throw new ApplicationExceptions.ExtractCorrespondentException(
                        String.format("Failed convert ID for correspondent: " + caseId), REPRESENTATIV_ID_INVALID);
            }
            log.debug("Case ID {} Complainent ID {} Representative ID {}", caseId, complainantId, representativeId);
        }
       if (complainantId.compareTo(representativeId) == 0){
           // complainent is primary correspondent

       } else {
           // representitive is primary correspondent
           individual = getCorrespondentDetails(representativeId);
           individual.setAddress(getAddress(representativeId));
       }

       log.debug("Representative {} data extracted", representativeId);
    }

    private Individual getCorrespondentDetails(BigDecimal partyId) {
        Individual individual = new Individual();
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
                individual.setDateOfBirth(ts);
            }
            Object nationality = result.get("nationality");
            if (nationality instanceof String s) {
                individual.setNationality(s);
            }
        }

        rs = namedParameterJdbcTemplate.queryForList(GET_CORRESPONDENT_PHONENUMBER, mapParameters);
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
        List<Reference> references = new ArrayList<>();
        for(Map row: rs) {
            Reference reference = new Reference();
            Object refType = row.get("reftype");
            if (refType instanceof String s) {
                reference.setRefType(s);
            }
            Object referenceType = row.get("reference");
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
