package uk.gov.digital.ho.hocs.cms.exception;

import com.microsoft.sqlserver.jdbc.SQLServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ExitCodeExceptionMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ExceptionMapper {

    @Bean
    public ExitCodeExceptionMapper exceptionBasedExitCode() {
        return exception -> {
            if (exception.getCause() instanceof SQLServerException sqlServerException) {
                log.error("SQL Server exception: {}, SQL Server state: {}", sqlServerException.getMessage(), sqlServerException.getSQLState());
                return 2;
            }
            return 99;
        };
    }
}
