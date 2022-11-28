package uk.gov.digital.ho.hocs.cms.complaints;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
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
public class ComplaintsExtractor {

    private final DataSource dataSource;
    private final String queryCaseIdsByDate = "SELECT caseid FROM FLODS_UKBACOMPLAINTS_D00 WHERE CREATED_DT BETWEEN ? AND ?";

    public ComplaintsExtractor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Integer> getCaseIdsByDateRange(String start, String end) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement stmt = connection.prepareStatement(queryCaseIdsByDate);
        LocalDate startDate = dateFormat(start);
        LocalDate endDate = dateFormat(end);
        stmt.setObject(1, startDate);
        stmt.setObject(2, endDate);
        ResultSet rs = stmt.executeQuery();
        List<Integer> res = new ArrayList<>();
        return res;
    }

    private LocalDate dateFormat(String strDate) {
        DateTimeFormatter dtf = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("MMM d,uuuu")
                .toFormatter(Locale.ENGLISH);
        return LocalDate.parse(strDate, dtf);
    }
}
