package uk.gov.digital.ho.hocs.cms.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean(name="cms")
    public DataSource dataSource(@Value("${spring.datasource.cms.driverClassName}") String driverClassName,
                                 @Value("${spring.datasource.cms.url}") String url,
                                 @Value("${spring.datasource.cms.username}") String userName,
                                 @Value("${spring.datasource.cms.password}") String password){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(userName);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean()
    @Primary
    public DataSource postgresDataSource(@Value("${spring.datasource.driverClassName}") String driverClassName,
                                 @Value("${spring.datasource.url}") String url){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        return dataSource;
    }

    @Bean(name="cms-template")
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(@Qualifier("cms") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

}
