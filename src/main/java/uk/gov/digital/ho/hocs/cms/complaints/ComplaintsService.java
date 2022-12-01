package uk.gov.digital.ho.hocs.cms.complaints;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.cms.documents.DocumentExtrator;
import uk.gov.digital.ho.hocs.cms.domain.ComplaintExtractRecord;
import uk.gov.digital.ho.hocs.cms.domain.repository.ComplaintsRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

@Service
@Slf4j
public class ComplaintsService {

    private final DocumentExtrator documentExtrator;
    private final ComplaintExtractor complaintsExtractor;
    private final ComplaintsRepository complaintsRepository;


    public ComplaintsService(DocumentExtrator documentExtrator, ComplaintExtractor complaintsExtractor, ComplaintsRepository complaintsRepository) {
        this.documentExtrator = documentExtrator;
        this.complaintsExtractor = complaintsExtractor;
        this.complaintsRepository = complaintsRepository;
    }

    public void migrate(String startDate, String endDate) throws SQLException {
        List<BigDecimal> complaints = complaintsExtractor.getComplaintIdsByDateRange(startDate, endDate);
        log.info("{} complaints found for dates {} to {}.", complaints.size(), startDate, endDate);
        for (BigDecimal complaint : complaints) {
            documentExtrator.copyDocumentsForCase(complaint.intValue());
            ComplaintExtractRecord record = new ComplaintExtractRecord();
            record.setCaseId(complaint.intValue());
            complaintsRepository.save(record);
        }
        log.info("Complaints extraction between dates {} and {} finished.", startDate, endDate);
    }
}
