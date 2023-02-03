package uk.gov.digital.ho.hocs.cms.categories;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.cms.domain.model.Categories;
import uk.gov.digital.ho.hocs.cms.domain.repository.CategoriesRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SubCategoriesExtractor {

    private final DataSource dataSource;

    private final JdbcTemplate jdbcTemplate;

    private final CategoriesRepository categoriesRepository;

    private final String FETCH_SELECTED_SUB_CATEGORIES = """
            SELECT sub_property_money, sub_clinical, sub_property_witheld, sub_rule_40_42, sub_property_stolen,
            sub_property_damaged, sub_pcpqueue_bf, sub_visits, sub_property_lost, sub_detainee_on_detainee FROM FLODS_UKBACOMPLAINTS_D00 WHERE caseid = ?
            """;

    public SubCategoriesExtractor(@Qualifier("cms") DataSource dataSource, CategoriesRepository categoriesRepository) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.categoriesRepository = categoriesRepository;
    }

    public void getSelectedSubCategoryData(BigDecimal caseId) {
        Map<String, String> selectedSubCategories = getSubCategories(caseId);
        if (selectedSubCategories == null) {
            log.error("No selected categories");
            return;
        }
        for (Map.Entry<String, String> entry : selectedSubCategories.entrySet()) {
            if (entry.getValue() != null) {
                String title = subCategoryTitles.get(entry.getKey());
                String checked = entry.getValue();
                if (checked.equalsIgnoreCase("Checked")) {
                    Categories category = new Categories();
                    category.setCaseId(caseId);
                    category.setCategory(title);
                    category.setSelected(checked);
                    categoriesRepository.save(category);
                }
            }
        }

    }

    private Map<String, String> getSubCategories(BigDecimal caseId) {
        Map<String, String> selectedSubCaqtegories = null;
        try {
            selectedSubCaqtegories = jdbcTemplate.queryForObject(FETCH_SELECTED_SUB_CATEGORIES, (rs, rowNum) -> {
                Map<String, String> cat = new HashMap<>();
                cat.put(SUB_PROPERTY_MONEY, rs.getString(SUB_PROPERTY_MONEY));
                cat.put(SUB_CLINICAL, rs.getString(SUB_CLINICAL));
                cat.put(SUB_PROPERTY_WITHHELD, rs.getString(SUB_PROPERTY_WITHHELD));
                cat.put(SUB_RULE_40_42, rs.getString(SUB_RULE_40_42));
                cat.put(SUB_PROPERTY_STOLEN, rs.getString(SUB_PROPERTY_STOLEN));
                cat.put(SUB_PROPERTY_DAMAGED, rs.getString(SUB_PROPERTY_DAMAGED));
                cat.put(SUB_PCPQUEUE_BF, rs.getString(SUB_PCPQUEUE_BF));
                cat.put(SUB_VISITS, rs.getString(SUB_VISITS));
                cat.put(SUB_PROPERTY_LOST, rs.getString(SUB_PROPERTY_LOST));
                cat.put(SUB_DETAINEE_ON_DETAINEE, rs.getString(SUB_DETAINEE_ON_DETAINEE));
                return cat;
            }, caseId);
        } catch (DataAccessException e) {
            log.info("No Sub Category selected for Case ID {}", caseId);
        }
        return selectedSubCaqtegories;
    }

    private final String SUB_PROPERTY_MONEY = "sub_property_money";
    private final String SUB_CLINICAL = "sub_clinical";
    private final String SUB_PROPERTY_WITHHELD = "sub_property_witheld";
    private final String SUB_RULE_40_42 = "sub_rule_40_42";
    private final String SUB_PROPERTY_STOLEN = "sub_property_stolen";
    private final String SUB_PROPERTY_DAMAGED = "sub_property_damaged";
    private final String SUB_PCPQUEUE_BF = "sub_pcpqueue_bf";
    private final String SUB_VISITS = "sub_visits";
    private final String SUB_PROPERTY_LOST = "sub_property_lost";
    private final String SUB_DETAINEE_ON_DETAINEE = "sub_detainee_on_detainee";

    private final Map<String, String> subCategoryTitles = Map.ofEntries(
            Map.entry(SUB_PROPERTY_MONEY, "Sub property money"),
            Map.entry(SUB_CLINICAL, "Sub clinical"),
            Map.entry(SUB_PROPERTY_WITHHELD, "Sub property withheld"),
            Map.entry(SUB_RULE_40_42, "Sub rule 40/42"),
            Map.entry(SUB_PROPERTY_STOLEN, "Sub property stolen"),
            Map.entry(SUB_PROPERTY_DAMAGED, "Sub property damaged"),
            Map.entry(SUB_PCPQUEUE_BF, "Sub PCP Queue BF"),
            Map.entry(SUB_VISITS, "Sub visits"),
            Map.entry(SUB_PROPERTY_LOST, "Sub property lost"),
            Map.entry(SUB_DETAINEE_ON_DETAINEE, "Sub detainee on detainee")
    );

}
