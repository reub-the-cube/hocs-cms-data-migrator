package uk.gov.digital.ho.hocs.cms.documents;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.cms.client.DocumentS3Client;
import uk.gov.digital.ho.hocs.cms.correspondents.CorrespondentType;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseAttachment;
import uk.gov.digital.ho.hocs.cms.domain.model.Individual;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseHistoryRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseLinksRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.CategoriesRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.CompensationRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.IndividualRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.ResponseRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.RiskAssessmentRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Component
@Slf4j
public class DocumentCreator {

    private final IndividualRepository individualRepository;
    private final CaseDataRepository caseDataRepository;
    private final CompensationRepository compensationRepository;
    private final CategoriesRepository categoriesRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final ResponseRepository responseRepository;
    private final CaseLinksRepository caseLinksRepository;
    private final CaseHistoryRepository caseHistoryRepository;
    private final DocumentS3Client documentS3Client;

    private final String CMS_CASE_DATA_FILENAME = "CMS_CASE_DATA";

    public DocumentCreator(IndividualRepository individualRepository,
                           CaseDataRepository caseDataRepository,
                           CompensationRepository compensationRepository,
                           CategoriesRepository categoriesRepository,
                           RiskAssessmentRepository riskAssessmentRepository,
                           ResponseRepository responseRepository,
                           CaseLinksRepository caseLinksRepository,
                           CaseHistoryRepository caseHistoryRepository,
                           DocumentS3Client documentS3Client) {
        this.individualRepository = individualRepository;
        this.caseDataRepository = caseDataRepository;
        this.compensationRepository = compensationRepository;
        this.categoriesRepository = categoriesRepository;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.responseRepository = responseRepository;
        this.caseLinksRepository = caseLinksRepository;
        this.caseHistoryRepository = caseHistoryRepository;
        this.documentS3Client = documentS3Client;
    }

    private final float fontSize = 12;
    private final float margin = 72;
    private final float leading = 1.5f * fontSize;
    private final PDFont font = PDType1Font.HELVETICA;

    @Transactional
    public CaseAttachment createDocument(BigDecimal caseId) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.setFont(PDType1Font.HELVETICA, fontSize);
        contentStream.setLeading(leading);

        List<BigDecimal> individualIds = individualRepository.findIndividualsByCaseId(caseId);
        Individual complainant = null;
        Individual representative = null;
        for (BigDecimal partyId : individualIds) {
            Optional<Individual> individual = individualRepository.findById(partyId);
            if (individual.isPresent()) {
                Individual ind = individual.get();
                if (ind.getPrimary() && ind.getType().equalsIgnoreCase(CorrespondentType.THIRD_PARTY_REP.toString())) {
                    representative = ind;
                } else if (ind.getType().equalsIgnoreCase(CorrespondentType.COMPLAINANT.toString())) {
                    complainant = ind;
                }
            }
        }
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
        contentStream.newLineAtOffset(100, 700);
        contentStream.showText("Personal Details");
        contentStream.setFont(PDType1Font.HELVETICA, fontSize);
        textForCorrespondent(contentStream, complainant);
        contentStream.endText();
        contentStream.close();

        if (representative != null) {
            page = new PDPage();
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 700);
            contentStream.setLeading(leading);
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
            contentStream.showText(String.format("Representative: %s", representative.getPartyId()));
            contentStream.setFont(PDType1Font.HELVETICA, fontSize);
            textForCorrespondent(contentStream, representative);
        }

        contentStream.endText();
        contentStream.close();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        document.close();
        byte[] pdfBytes = baos.toByteArray();

        String tempFileName = documentS3Client.storeUntrustedDocument(CMS_CASE_DATA_FILENAME, pdfBytes, caseId);

        CaseAttachment caseAttachment = new CaseAttachment();
        caseAttachment.setDocumentPath(tempFileName);
        caseAttachment.setDocumentType("PDF");
        caseAttachment.setDisplayName(CMS_CASE_DATA_FILENAME);
        return caseAttachment;
    }

        private void textForCorrespondent(PDPageContentStream contentStream, Individual complainant) throws IOException {
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText((String.format("Complainant: %s", complainant.getPartyId())));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Forename: %s", complainant.getForename()));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Surname: %s", complainant.getSurname()));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Date of birth: %s", complainant.getDateOfBirth()));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Nationality: %s", complainant.getNationality()));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Telephone: %s", complainant.getTelephone()));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Email: %s", complainant.getEmail()));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
            contentStream.showText("Address");
            contentStream.setFont(PDType1Font.HELVETICA, fontSize);
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("House name/number: %s", complainant.getAddress().getNumber()));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Address line 1: %s", complainant.getAddress().getAddressLine1()));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Address Line 2: %s", complainant.getAddress().getAddressLine2()));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Address Line 3: %s", complainant.getAddress().getAddressLine3()));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Address Line 4: %s", complainant.getAddress().getAddressLine4()));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Address Line 5: %s", complainant.getAddress().getAddressLine5()));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Address Line 6: %s", complainant.getAddress().getAddressLine6()));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Postcode: %s", complainant.getAddress().getPostcode()));

        }


}
