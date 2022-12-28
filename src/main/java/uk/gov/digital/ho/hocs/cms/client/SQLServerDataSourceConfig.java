package uk.gov.digital.ho.hocs.cms.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class SQLServerDataSourceConfig {

    @Bean(name="sqlServerDataSourceProperties")
    @ConfigurationProperties("spring.datasource.cms")
    public DataSourceProperties sqlServerDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource sqlServerDataSource(@Qualifier("sqlServerDataSourceProperties") DataSourceProperties sqlServerDataSourceProperties) {
        return sqlServerDataSourceProperties
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean(name="cms-template")
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(@Qualifier("sqlServerDataSource") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

}
