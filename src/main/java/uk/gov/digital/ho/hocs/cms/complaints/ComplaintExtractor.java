package uk.gov.digital.ho.hocs.cms.complaints;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class ComplaintExtractor {

    private final DataSource dataSource;
    private final String COMPLAINT_ID_BY_DATE_RANGE = "SELECT caseid FROM FLODS_UKBACOMPLAINTS_D00 WHERE CREATED_DT BETWEEN ? AND ?";

    public ComplaintExtractor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<BigDecimal> getComplaintIdsByDateRange(String start, String end) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement stmt = connection.prepareStatement(COMPLAINT_ID_BY_DATE_RANGE);
        stmt.setString(1, start);
        stmt.setString(2, end);
        ResultSet rs = stmt.executeQuery();
        List<BigDecimal> cases = new ArrayList<>();
        while (rs.next()) {
            cases.add(rs.getBigDecimal(1));
        }
        return cases;
    }

    private LocalDate dateFormat(String strDate) {
        DateTimeFormatter dtf = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("MMM d,uuuu")
                .toFormatter(Locale.ENGLISH);
        return LocalDate.parse(strDate, dtf);
    }
}
