package uk.gov.digital.ho.hocs.cms.categories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent;
import uk.gov.digital.ho.hocs.cms.domain.model.Categories;
import uk.gov.digital.ho.hocs.cms.domain.repository.CategoriesRepository;

import javax.sql.DataSource;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent.DOCUMENT_RETRIEVAL_FAILED;

@Component
@Slf4j
public class CategoriesExtractor {

    private final DataSource dataSource;

    private final JdbcTemplate jdbcTemplate;

    private final CategoriesRepository categoriesRepository;

    private final String FETCH_SELECTED_CATEGORIES = """
            SELECT delay_, adminprocesserror, poorcommunication, wronginformation, lostdocuments, ccphysicalenvironment, ccavailabilityofservice,
            ccprovisionforminors, cccomplainthandling, rudeness, otherunprofessional, unfairtreatment, theft, assault, sexualassault,
            fraud, racism, damage_bf, custody_bf FROM FLODS_UKBACOMPLAINTS_D00 WHERE caseid = ?
            """;

    public CategoriesExtractor(@Qualifier("cms") DataSource dataSource, CategoriesRepository categoriesRepository) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.categoriesRepository = categoriesRepository;
    }

    @Transactional
    public void getSelectedCategoryData(BigDecimal caseId) {
        categoriesRepository.deleteAllByCaseId(caseId);
        Map<String, String> selectedCategories = getSelectedCategories(caseId);
        if (selectedCategories == null) {
            log.error("No selected categories");
            return;
        }
        for (Map.Entry<String, String> entry : selectedCategories.entrySet()) {
            if (entry.getValue() != null) {
                String checked = entry.getValue();
                if(checked.equalsIgnoreCase("Checked")) {
                    Map<String, String> catDetails = categoryValues.get(entry.getKey());
                    getSelectedCategoryData(entry.getKey(), catDetails.get(SELECTED), catDetails.get(SUBSTANTIATED), catDetails.get(AMOUNT), caseId);
                }
            }
        }

    }

    private void getSelectedCategoryData(String categoryName, String selected, String substantiated, String amount, BigDecimal caseId) {
        String FETCH_CATEGORY_DATA = String.format("SELECT %s, %s, %s FROM FLODS_UKBACOMPLAINTS_D00 WHERE caseid = ?", selected, substantiated, amount);
        try {
            Categories category = jdbcTemplate.queryForObject(FETCH_CATEGORY_DATA, (rs, rowNum) -> {
                Categories categories = new Categories();
                categories.setSelected(rs.getString(selected));
                categories.setSubstantiated(rs.getString(substantiated));
                categories.setAmount(rs.getBigDecimal(amount));
                return categories;
            }, caseId);
            category.setCaseId(caseId);
            category.setCategory(categoryTitles.get(categoryName));
            categoriesRepository.save(category);
        } catch (DataAccessException e) {
            log.error("Couldn't retrieve category data for CASE ID {}. Error message: {}", caseId, e.getMessage());
            throw new ApplicationExceptions.ExtractCategoriesException(e.getMessage(), LogEvent.CATEGORIES_EXTRACT_FAILED);

        }
    }

    private final String DELAY = "delay_";
    private final String ADMIN_PROCESS_ERROR = "adminprocesserror";
    private final String POOR_COMMUNICATION = "poorcommunication";
    private final String WRONG_INFORMATION = "wronginformation";
    private final String LOST_DOCUMENTS = "lostdocuments";
    private final String CC_PHYSICAL_ENVIRONMENT = "ccphysicalenvironment";
    private final String CC_AVAILABILITY_OF_SERVICE = "ccavailabilityofservice";
    private final String CC_PROVISION_FOR_MINROS = "ccprovisionforminors";
    private final String CC_COMPLAINT_HANDLING = "cccomplainthandling";
    private final String RUDENESS = "rudeness";
    private final String OTHER_NON_PROFESSIONAL = "otherunprofessional";
    private final String UNFAIR_TREATMENT = "unfairtreatment";
    private final String THEFT = "theft";
    private final String ASSAULT = "assault";
    private final String SEXUAL_ASSAULT = "sexualassault";
    private final String FRAUD = "fraud";
    private final String RACISM = "racism";
    private final String DAMAGE_BF = "damage_bf";
    private final String CUSTODY_BF = "custody_bf";

    private final Map<String, String> categoryTitles = Map.ofEntries(
            Map.entry(DELAY, "Delay"),
            Map.entry(ADMIN_PROCESS_ERROR, "Admin process error"),
            Map.entry(POOR_COMMUNICATION, "Poor communication"),
            Map.entry(WRONG_INFORMATION, "Wrong information"),
            Map.entry(LOST_DOCUMENTS, "Lost documents"),
            Map.entry(CC_PHYSICAL_ENVIRONMENT, "CC Physical environment"),
            Map.entry(CC_AVAILABILITY_OF_SERVICE, "CC Availability of service"),
            Map.entry(CC_PROVISION_FOR_MINROS, "CC Provision for minors"),
            Map.entry(CC_COMPLAINT_HANDLING, "CC Complaint handling"),
            Map.entry(RUDENESS, "Rudeness"),
            Map.entry(OTHER_NON_PROFESSIONAL, "Other non professional"),
            Map.entry(UNFAIR_TREATMENT, "Unfair treatment"),
            Map.entry(THEFT, "Theft"),
            Map.entry(ASSAULT, "Assault"),
            Map.entry(SEXUAL_ASSAULT, "Sexual assault"),
            Map.entry(FRAUD, "Fraud"),
            Map.entry(RACISM, "Racism"),
            Map.entry(DAMAGE_BF, "Damage BF"),
            Map.entry(CUSTODY_BF, "Custom BF")
    );


    private Map<String, String> getSelectedCategories(BigDecimal caseId) {
        Map<String, String> selectedCaqtegories = null;
        try {
            selectedCaqtegories = jdbcTemplate.queryForObject(FETCH_SELECTED_CATEGORIES, (rs, rowNum) -> {
                Map<String, String> cat = new HashMap<>();
                cat.put(DELAY, rs.getString(DELAY));
                cat.put(ADMIN_PROCESS_ERROR, rs.getString(ADMIN_PROCESS_ERROR));
                cat.put(POOR_COMMUNICATION, rs.getString(POOR_COMMUNICATION));
                cat.put(WRONG_INFORMATION, rs.getString(WRONG_INFORMATION));
                cat.put(LOST_DOCUMENTS, rs.getString(LOST_DOCUMENTS));
                cat.put(CC_PHYSICAL_ENVIRONMENT, rs.getString(CC_PHYSICAL_ENVIRONMENT));
                cat.put(CC_AVAILABILITY_OF_SERVICE, rs.getString(CC_AVAILABILITY_OF_SERVICE));
                cat.put(CC_PROVISION_FOR_MINROS, rs.getString(CC_PROVISION_FOR_MINROS));
                cat.put(CC_COMPLAINT_HANDLING, rs.getString(CC_COMPLAINT_HANDLING));
                cat.put(RUDENESS, rs.getString(RUDENESS));
                cat.put(OTHER_NON_PROFESSIONAL, rs.getString(OTHER_NON_PROFESSIONAL));
                cat.put(UNFAIR_TREATMENT, rs.getString(UNFAIR_TREATMENT));
                cat.put(THEFT, rs.getString(THEFT));
                cat.put(ASSAULT, rs.getString(ASSAULT));
                cat.put(SEXUAL_ASSAULT, rs.getString(SEXUAL_ASSAULT));
                cat.put(FRAUD, rs.getString(FRAUD));
                cat.put(RACISM, rs.getString(RACISM));
                cat.put(DAMAGE_BF, rs.getString(DAMAGE_BF));
                cat.put(CUSTODY_BF, rs.getString(CUSTODY_BF));
                return cat;
            }, caseId);
        } catch (DataAccessException e) {
            log.info("No Category selected for Case ID {}", caseId);
        }
        return selectedCaqtegories;
    }
    
    private final Map<String, Map<String, String>> categoryValues = Map.ofEntries(
        Map.entry(DELAY, getCategoryColumnNames("delay_","delay_status", "delay_amount")),
        Map.entry(ADMIN_PROCESS_ERROR, getCategoryColumnNames("adminprocesserror", "adminprocesserror_status", "adminprocesserror_amount")),
        Map.entry(POOR_COMMUNICATION, getCategoryColumnNames("poorcommunication", "poorcommunication_status", "poorcommunication_amount")),
        Map.entry(WRONG_INFORMATION, getCategoryColumnNames("wronginformation", "wronginformation_status", "wronginformation_amount")),
        Map.entry(LOST_DOCUMENTS, getCategoryColumnNames("lostdocuments", "lostdocuments_status", "lostdocuments_amount")),
        Map.entry(CC_PHYSICAL_ENVIRONMENT, getCategoryColumnNames("ccphysicalenvironment", "ccphysicalenvironment_status", "ccphysicalenvironment_amount")),
        Map.entry(CC_AVAILABILITY_OF_SERVICE, getCategoryColumnNames("ccavailabilityofservice", "ccavailabilityofservice_status", "ccavailabilityofservice_amount")),
        Map.entry(CC_PROVISION_FOR_MINROS, getCategoryColumnNames("ccprovisionforminors", "ccprovisionforminors_status", "ccprovisionforminors_amount")),
        Map.entry(CC_COMPLAINT_HANDLING, getCategoryColumnNames("cccomplainthandling", "cccomplainthandling_status", "cccomplainthandling_amount")),
        Map.entry(RUDENESS, getCategoryColumnNames("rudeness", "rudeness_status", "rudeness_amount")),
        Map.entry(OTHER_NON_PROFESSIONAL, getCategoryColumnNames("otherunprofessional", "otherunprofessional_status", "otherunprofessional_amount")),
        Map.entry(UNFAIR_TREATMENT, getCategoryColumnNames("unfairtreatment", "unfairtreatment_status", "unfairtreatment_amount")),
        Map.entry(THEFT, getCategoryColumnNames("theft", "theft_status", "theft_amount")),
        Map.entry(ASSAULT, getCategoryColumnNames("assault", "assault_status", "assault_amount")),
        Map.entry(SEXUAL_ASSAULT, getCategoryColumnNames("sexualassault", "sexualassault_status", "sexualassault_amount")),
        Map.entry(FRAUD, getCategoryColumnNames("fraud", "fraud_status", "fraud_amount")),
        Map.entry(RACISM, getCategoryColumnNames("racism", "racism_status", "racism_amount")),
        Map.entry(DAMAGE_BF, getCategoryColumnNames( "damage_bf", "damage_bf_status", "damage_bf_amount")),
        Map.entry(CUSTODY_BF, getCategoryColumnNames("custody_bf", "custody_bf_status", "custody_bf_amount"))
    );

    private final String SELECTED = "selected";
    private final String SUBSTANTIATED = "substantiated";
    private final String AMOUNT = "amount";

    private Map<String, String> getCategoryColumnNames(String selected, String substantiated, String amount) {
        Map<String, String> categoryColumnNames = new HashMap<>();
        categoryColumnNames.put(SELECTED, selected);
        categoryColumnNames.put(SUBSTANTIATED, substantiated);
        categoryColumnNames.put(AMOUNT, amount);
        return categoryColumnNames;
    }

}
