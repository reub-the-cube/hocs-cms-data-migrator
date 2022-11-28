package uk.gov.digital.ho.hocs.cms.complaints;

import java.sql.Date;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class ExtractCaseIdsByDateRange {

    private final DataSource dataSource;
    private final String queryCaseIdsByDate = "SELECT caseid FROM FLODS_UKBACOMPLAINTS_D00 WHERE CREATED_DT BETWEEN ? AND ?";

    public ExtractCaseIdsByDateRange(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Integer> getCaseIds(String start, String end) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement stmt = connection.prepareStatement(queryCaseIdsByDate);
        LocalDate startDate = dateFormat("jan 25,2021");
        LocalDate endDate = dateFormat("feb 28 2021");
        stmt.setObject(1, startDate);
        stmt.setObject(2, endDate);
        List<Integer> res = new ArrayList<>();
        return res;
    }

    private LocalDate dateFormat(String strDate) {
        DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("MMM dd,yyyy", Locale.ENGLISH);
        DateTimeFormatter dtf = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("MMM d,uuuu")
                .toFormatter(Locale.ENGLISH);

        LocalDate date = LocalDate.parse(strDate, dtf);
        System.out.println(date);
        return date;
    }



}
