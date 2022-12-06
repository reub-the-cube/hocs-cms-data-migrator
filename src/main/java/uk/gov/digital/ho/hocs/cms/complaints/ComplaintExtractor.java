package uk.gov.digital.ho.hocs.cms.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.cms.documents.DocumentExtrator;
import uk.gov.digital.ho.hocs.cms.domain.ComplaintExtractRecord;

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
@Slf4j
public class ComplaintExtractor {

    private final DataSource dataSource;
    private final DocumentExtrator documentExtrator;
    private final String COMPLAINT_ID_BY_DATE_RANGE = "SELECT caseid FROM FLODS_UKBACOMPLAINTS_D00 WHERE CREATED_DT BETWEEN ? AND ?";

    public ComplaintExtractor(DataSource dataSource, DocumentExtrator documentExtrator) {
        this.dataSource = dataSource;
        this.documentExtrator = documentExtrator;
    }

    public List<BigDecimal> getComplaintIdsByDateRange(String start, String end) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(COMPLAINT_ID_BY_DATE_RANGE);
        ps.setString(1, start);
        ps.setString(2, end);
        ResultSet rs = ps.executeQuery();
        List<BigDecimal> cases = new ArrayList<>();
        while (rs.next()) {
            cases.add(rs.getBigDecimal(1));
        }
        ps.close();
        conn.close();
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
