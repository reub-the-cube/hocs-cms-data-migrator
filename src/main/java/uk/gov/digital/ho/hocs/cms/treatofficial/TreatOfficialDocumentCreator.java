package uk.gov.digital.ho.hocs.cms.treatofficial;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.datatable.DataTable;
import com.google.common.base.CharMatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseAttachment;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseLinks;
import uk.gov.digital.ho.hocs.cms.domain.model.ComplaintCase;
import uk.gov.digital.ho.hocs.cms.domain.model.CorrespondentTreatOfficial;
import uk.gov.digital.ho.hocs.cms.domain.model.Individual;
import uk.gov.digital.ho.hocs.cms.domain.model.Reference;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseLinksRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.CasesRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.IndividualRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.TreatOfficialCorrespondentsRepository;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class TreatOfficialDocumentCreator {

    private final IndividualRepository individualRepository;
    private final TreatOfficialCorrespondentsRepository treatOfficialCorrespondentsRepository;
    private final CaseLinksRepository caseLinksRepository;


    private final String CMS_CASE_DATA_FILENAME = "CMS_CASE_DATA.pdf";

    public TreatOfficialDocumentCreator(IndividualRepository individualRepository,
                                        TreatOfficialCorrespondentsRepository treatOfficialCorrespondentsRepository,
                                        CaseLinksRepository caseLinksRepository) {
        this.individualRepository = individualRepository;
        this.treatOfficialCorrespondentsRepository = treatOfficialCorrespondentsRepository;
        this.caseLinksRepository = caseLinksRepository;
    }

    private final float fontSize = 12;
    private final float margin = 50;
    private final float leading = 1.5f * fontSize;
    private final PDFont normalFont = PDType1Font.COURIER;
    private final PDFont boldFont = PDType1Font.COURIER_BOLD;

    public CaseAttachment createDocument(BigDecimal caseId) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(normalFont, fontSize);
            contentStream.setLeading(leading);

            List<CorrespondentTreatOfficial> correspondentTreatOfficials = treatOfficialCorrespondentsRepository.findByCaseId(caseId);
            log.info("Fetched {} correspondents for case ID {}", correspondentTreatOfficials.size(), caseId );
            Individual primaryCorrespondent = null;
            List<Individual> representatives = new ArrayList<>();
            for (CorrespondentTreatOfficial correspondentTreatOfficial : correspondentTreatOfficials) {
                Optional<Individual> correspondentOptional = individualRepository.findById(correspondentTreatOfficial.getCorrespondentId());
                if (correspondentTreatOfficial.getIsPrimary()) {
                    primaryCorrespondent = correspondentOptional.get();
                } else {
                    representatives.add(correspondentOptional.get());
                }
            }
            contentStream.beginText();
            contentStream.setFont(boldFont, fontSize);
            contentStream.newLineAtOffset(margin, 700);
            contentStream.showText("Personal Details");
            contentStream.setFont(normalFont, fontSize);
            textForCorrespondent(contentStream, primaryCorrespondent);
            contentStream.endText();
            contentStream.close();
            // references for complainant
            page = new PDPage();
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, 700);
            BaseTable complainantRefsTable = new BaseTable(680, 700, 20, 500, margin, document, page, true, true);
            contentStream.setFont(boldFont, fontSize);
            contentStream.showText("References");
            contentStream.setFont(normalFont, fontSize);
            contentStream.newLineAtOffset(0, -leading);
            List<List> data = getReferences(primaryCorrespondent);
            DataTable t = new DataTable(complainantRefsTable, page);
            t.addListToTable(data, DataTable.HASHEADER);
            complainantRefsTable.draw();
            contentStream.endText();
            contentStream.close();

            for (Individual individual : representatives) {
                page = new PDPage();
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, 700);
                contentStream.setLeading(leading);
                contentStream.setFont(boldFont, fontSize);
                contentStream.showText(String.format("Representative: %s", individual.getPartyId()));
                contentStream.setFont(normalFont, fontSize);
                textForCorrespondent(contentStream, individual);
                contentStream.endText();
                contentStream.close();
                // references for representative
                page = new PDPage();
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, 700);
                BaseTable representativeRefsTable = new BaseTable(680, 700, 20, 500, margin, document, page, true, true);
                contentStream.setFont(boldFont, fontSize);
                contentStream.showText("References");
                contentStream.setFont(normalFont, fontSize);
                contentStream.newLineAtOffset(0, -leading);
                data = getReferences(individual);
                DataTable representativeDataTable = new DataTable(representativeRefsTable, page);
                representativeDataTable.addListToTable(data, DataTable.HASHEADER);
                representativeRefsTable.draw();
                contentStream.endText();
                contentStream.close();
            }

            // case links
            List<CaseLinks> caseLinks = caseLinksRepository.findAllBySourceCaseId(caseId);
            caseLinks.addAll(caseLinksRepository.findAllByTargetCaseId(caseId));
            page = new PDPage();
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, 700);
            contentStream.setFont(boldFont, fontSize);
            contentStream.showText("Case Links - lgncc_caselink");
            contentStream.setFont(normalFont, fontSize);
            BaseTable caseLinksTable = new BaseTable(680, 700, 20, 500, margin, document, page, true,
                    true);

            List<List> caseLinksData = new ArrayList<>();
            caseLinksData.add(new ArrayList<>(Arrays.asList("Source case", "Link type", "Target case")));

            for (CaseLinks caseLink : caseLinks) {
                caseLinksData.add(new ArrayList(Arrays.asList(removeInvalidChars(caseLink.getSourceCaseId().toString()),
                        removeInvalidChars(caseLink.getDescription()),
                        removeInvalidChars(caseLink.getTargetCaseId().toString()))));
            }
            DataTable caseLinksDataTable = new DataTable(caseLinksTable, page);
            caseLinksDataTable.addListToTable(caseLinksData, DataTable.HASHEADER);
            caseLinksTable.draw();
            contentStream.endText();
            contentStream.close();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            document.close();
            byte[] pdfBytes = baos.toByteArray();

        } catch (IOException e) {
            throw new ApplicationExceptions.CreateMigrationDocumentException(e.getMessage(),
                    LogEvent.MIGRATION_DOCUMENT_FAILED);
        }

        CaseAttachment caseAttachment = new CaseAttachment();
        return caseAttachment;

    }

    private void textForCorrespondent(PDPageContentStream contentStream, Individual complainant) throws IOException {
        contentStream.newLineAtOffset(0, -leading);
        contentStream.showText((String.format("Complainant: %s", complainant.getPartyId())));
        contentStream.newLineAtOffset(0, -leading);
        contentStream.showText(String.format("Forename: %s", removeInvalidChars(complainant.getForename())));
        contentStream.newLineAtOffset(0, -leading);
        contentStream.showText(String.format("Surname: %s", removeInvalidChars(complainant.getSurname())));
        contentStream.newLineAtOffset(0, -leading);
        contentStream.showText(String.format("Date of birth: %s", complainant.getDateOfBirth()));
        contentStream.newLineAtOffset(0, -leading);
        contentStream.showText(String.format("Nationality: %s", removeInvalidChars(complainant.getNationality())));
        contentStream.newLineAtOffset(0, -leading);
        contentStream.showText(String.format("Telephone: %s", removeInvalidChars(complainant.getTelephone())));
        contentStream.newLineAtOffset(0, -leading);
        contentStream.showText(String.format("Email: %s", removeInvalidChars(complainant.getEmail())));
        contentStream.newLineAtOffset(0, -leading);
        contentStream.setFont(boldFont, fontSize);
        contentStream.showText("Address");
        contentStream.setFont(normalFont, fontSize);
        contentStream.newLineAtOffset(0, -leading);
        contentStream.showText(String.format("House name/number: %s", removeInvalidChars(complainant.getAddress().getNumber())));
        contentStream.newLineAtOffset(0, -leading);
        contentStream.showText(String.format("Address line 1: %s", removeInvalidChars(complainant.getAddress().getAddressLine1())));
        contentStream.newLineAtOffset(0, -leading);
        contentStream.showText(String.format("Address Line 2: %s", removeInvalidChars(complainant.getAddress().getAddressLine2())));
        contentStream.newLineAtOffset(0, -leading);
        contentStream.showText(String.format("Address Line 3: %s", removeInvalidChars(complainant.getAddress().getAddressLine3())));
        contentStream.newLineAtOffset(0, -leading);
        contentStream.showText(String.format("Address Line 4: %s", removeInvalidChars(complainant.getAddress().getAddressLine4())));
        contentStream.newLineAtOffset(0, -leading);
        contentStream.showText(String.format("Address Line 5: %s", removeInvalidChars(complainant.getAddress().getAddressLine5())));
        contentStream.newLineAtOffset(0, -leading);
        contentStream.showText(String.format("Address Line 6: %s", removeInvalidChars(complainant.getAddress().getAddressLine6())));
        contentStream.newLineAtOffset(0, -leading);
        contentStream.showText(String.format("Postcode: %s", removeInvalidChars(complainant.getAddress().getPostcode())));
    }

    private List<List> getReferences(Individual individual) {
        List<List> data = new ArrayList();
        data.add(new ArrayList<>(Arrays.asList("Reference type", "Reference")));
        List<Reference> references = individual.getReferences();
        for (Reference reference : references) {
            data.add(new ArrayList(Arrays.asList(removeInvalidChars(reference.getRefType()),
                    removeInvalidChars(reference.getReference()))));
        }
        return data;
    }



    private String removeInvalidChars(String s) {
        String result = "";
        if (s != null) {
            result = CharMatcher.ASCII.retainFrom(s);
            result = CharMatcher.WHITESPACE.trimTrailingFrom(result);
            result = CharMatcher.WHITESPACE.replaceFrom(result, " ");
            result = CharMatcher.JAVA_ISO_CONTROL.removeFrom(result);
            result = result.replaceAll("\n", "").replaceAll("\r", "").replaceAll("\t", " ");
            result = result.replace("\"", "'");
        }
        return result;
    }


    }
