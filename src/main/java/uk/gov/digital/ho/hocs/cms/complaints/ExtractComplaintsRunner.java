package uk.gov.digital.ho.hocs.cms.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.util.List;
import java.util.Map;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "cms.extract.complaints", havingValue = "enabled", matchIfMissing = false)
public class ExtractComplaintsRunner implements CommandLineRunner {

    private final ApplicationContext applicationContext;
    private final JdbcTemplate jdbcTemplate;

    public ExtractComplaintsRunner(ApplicationContext applicationContext, JdbcTemplate jdbcTemplate) {
        this.applicationContext = applicationContext;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        log.info("Extract complaints started");
        SqlRowSet res = jdbcTemplate.queryForRowSet("SELECT name, database_id, create_date FROM sys.databases;");
        List<Map<String, Object>> result = jdbcTemplate.queryForList(
                "SELECT name, database_id, create_date FROM sys.databases;");
        int rowCount = 0;
        while (res.next()) {
            System.out.println(res.getString("name") + " - "
                    + res.getString("database_id") + " - "
                    + res.getString("create_date"));
            rowCount++;
        }
        System.out.println("Number of records : " + rowCount);
        log.info("Migration completed successfully, exiting");
//        System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }
}
