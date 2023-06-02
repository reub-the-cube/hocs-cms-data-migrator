package uk.gov.digital.ho.hocs.cms.documents;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.datatable.DataTable;
import com.google.common.base.CharMatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.WordUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.cms.client.DocumentS3Client;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent;
import uk.gov.digital.ho.hocs.cms.domain.message.CaseAttachment;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseDataComplaint;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseHistory;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseLinks;
import uk.gov.digital.ho.hocs.cms.domain.model.Categories;
import uk.gov.digital.ho.hocs.cms.domain.model.Compensation;
import uk.gov.digital.ho.hocs.cms.domain.model.ComplaintCase;
import uk.gov.digital.ho.hocs.cms.domain.model.Individual;
import uk.gov.digital.ho.hocs.cms.domain.model.Reference;
import uk.gov.digital.ho.hocs.cms.domain.model.Response;
import uk.gov.digital.ho.hocs.cms.domain.model.RiskAssessment;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseDataComplaintsRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseHistoryRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseLinksRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.CasesRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.CategoriesRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.CompensationRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.IndividualRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.ResponseRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.RiskAssessmentRepository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Component
@Slf4j
public class DocumentCreator {

    private final IndividualRepository individualRepository;
    private final CaseDataComplaintsRepository caseDataComplaintsRepository;
    private final CompensationRepository compensationRepository;
    private final CategoriesRepository categoriesRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final ResponseRepository responseRepository;
    private final CaseLinksRepository caseLinksRepository;
    private final CaseHistoryRepository caseHistoryRepository;
    private final CasesRepository casesRepository;
    private final DocumentS3Client documentS3Client;

    private final String CMS_CASE_DATA_FILENAME = "CMS_CASE_DATA.pdf";

    public DocumentCreator(IndividualRepository individualRepository,
                           CaseDataComplaintsRepository caseDataComplaintsRepository,
                           CompensationRepository compensationRepository,
                           CategoriesRepository categoriesRepository,
                           RiskAssessmentRepository riskAssessmentRepository,
                           ResponseRepository responseRepository,
                           CaseLinksRepository caseLinksRepository,
                           CaseHistoryRepository caseHistoryRepository,
                           CasesRepository casesRepository,
                           DocumentS3Client documentS3Client) {
        this.individualRepository = individualRepository;
        this.caseDataComplaintsRepository = caseDataComplaintsRepository;
        this.compensationRepository = compensationRepository;
        this.categoriesRepository = categoriesRepository;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.responseRepository = responseRepository;
        this.caseLinksRepository = caseLinksRepository;
        this.caseHistoryRepository = caseHistoryRepository;
        this.casesRepository = casesRepository;
        this.documentS3Client = documentS3Client;
    }

    private final float fontSize = 12;
    private final float margin = 50;
    private final float leading = 1.5f * fontSize;
    private final PDFont normalFont = PDType1Font.COURIER;
    private final PDFont boldFont = PDType1Font.COURIER_BOLD;

    @Transactional
    public CaseAttachment createDocument(BigDecimal caseId) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(normalFont, fontSize);
            contentStream.setLeading(leading);

            ComplaintCase complaintCase = casesRepository.findByCaseId(caseId);
            Optional<Individual> complainantOptional = individualRepository.findById(complaintCase.getComplainantId());
            Optional<Individual> representativeOptional = individualRepository.findById(complaintCase.getRepresentativeId());
            Individual complainant = null;
            if (complainantOptional.isPresent()) {
                complainant = complainantOptional.get();
            } else {
                throw new ApplicationExceptions.CreateMigrationDocumentException(
                        String.format("Complainant doesn't exist. Complainant ID {}", complaintCase.getComplainantId()),
                        LogEvent.MIGRATION_DOCUMENT_FAILED);
            }
            Individual representative = null;
            if (representativeOptional.isPresent()) {
                representative = representativeOptional.get();
            }

            contentStream.beginText();
            contentStream.setFont(boldFont, fontSize);
            contentStream.newLineAtOffset(margin, 700);
            contentStream.showText("Personal Details");
            contentStream.setFont(normalFont, fontSize);
            textForCorrespondent(contentStream, complainant);
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
            List<List> data = getReferences(complainant);
            DataTable t = new DataTable(complainantRefsTable, page);
            t.addListToTable(data, DataTable.HASHEADER);
            complainantRefsTable.draw();
            contentStream.endText();
            contentStream.close();

            if (representative != null && representative.getPrimary()) {
                page = new PDPage();
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, 700);
                contentStream.setLeading(leading);
                contentStream.setFont(boldFont, fontSize);
                contentStream.showText(String.format("Representative: %s", representative.getPartyId()));
                contentStream.setFont(normalFont, fontSize);
                textForCorrespondent(contentStream, representative);
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
                data = getReferences(representative);
                DataTable representativeDataTable = new DataTable(representativeRefsTable, page);
                representativeDataTable.addListToTable(data, DataTable.HASHEADER);
                representativeRefsTable.draw();
                contentStream.endText();
                contentStream.close();
            }

            // Add case data to document
            CaseDataComplaint casedata = caseDataComplaintsRepository.findByCaseId(caseId);
            page = new PDPage();
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, 700);
            contentStream.setLeading(leading);
            contentStream.setFont(boldFont, fontSize);
            contentStream.showText("Case Data");
            contentStream.setFont(normalFont, fontSize);
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Reference: %s", removeInvalidChars(casedata.getCaseReference())));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Date Received: %s", removeInvalidChars(casedata.getReceiveDate())));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Due Date: %s", removeInvalidChars(casedata.getSlaDate())));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Initial Type: %s", removeInvalidChars(casedata.getInitialType())));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Current Type: %s", removeInvalidChars(casedata.getCurrentType())));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Description: "));
            String[] description = makeParagraph(casedata.getDescription());
            for (int i=0; i< description.length; i++) {
                contentStream.showText(description[i]);
                contentStream.newLineAtOffset(0, -leading);
            }
            contentStream.showText(String.format("Current Work Queue: %s", removeInvalidChars(casedata.getQueueName())));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Location: %s", removeInvalidChars(casedata.getLocation())));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("NRO: %s", removeInvalidChars(casedata.getNroCombo())));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Closed Date: %s", removeInvalidChars(casedata.getClosedDt())));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Severity: %s", removeInvalidChars(casedata.getSeverity().toString())));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Channel: %s", removeInvalidChars(casedata.getChannel().toString())));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Owning CSU: %s", removeInvalidChars(casedata.getOwningCsu())));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Business Area: %s", removeInvalidChars(casedata.getBusinessArea())));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Status: %s", removeInvalidChars(casedata.getStatus())));
            contentStream.endText();
            contentStream.close();

            // Add compensation data to document
            Compensation compensation = compensationRepository.findByCaseId(caseId);
            page = new PDPage();
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, 700);
            contentStream.setFont(boldFont, fontSize);
            contentStream.showText("Compensation");
            contentStream.setFont(normalFont, fontSize);
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Date of Compensation Claim: %s", compensation.getDateOfCompensationClaim()));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Offer Accepted: %s", compensation.getOfferAccepted()));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Date of Payment: %s", compensation.getDateOfPayment()));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Compensation Amount: %s", compensation.getCompensationAmmount()));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Amount Claimed: %s", compensation.getAmountClaimed()));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Amount Offered: %s", compensation.getAmountOffered()));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Consolatory Payment: %s", compensation.getConsolatoryPayment()));
            contentStream.endText();
            contentStream.close();

            // Add categories to document
            page = new PDPage();
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, 700);
            contentStream.setFont(normalFont, fontSize);
            contentStream.showText("Complaint category breakdown");
            contentStream.setFont(normalFont, fontSize);

            BaseTable categoryTable = new BaseTable(680, 700, 20, 500, margin, document, page, true,
                    true);

            List<List> categoryData = new ArrayList();
            categoryData.add(new ArrayList<>(Arrays.asList("Category", "Selected", "Substantiated", "Amount")));
            List<Categories> categories = categoriesRepository.findAllByCaseId(caseId);
            for (Categories category : categories) {
                String amount = "";
                if (category.getAmount() != null) amount = removeInvalidChars(category.getAmount().toString());
                String substantiated = "";
                if (category.getSubstantiated() != null) substantiated = removeInvalidChars(category.getSubstantiated());
                categoryData.add(new ArrayList(Arrays.asList(removeInvalidChars(category.getCategory()),
                        removeInvalidChars(category.getSelected()), substantiated, amount)));
            }

            DataTable categoryDataTable = new DataTable(categoryTable, page);
            categoryDataTable.addListToTable(categoryData, DataTable.HASHEADER);
            categoryTable.draw();
            contentStream.endText();
            contentStream.close();

            // risk assessment
            RiskAssessment riskAssessment = riskAssessmentRepository.findByCaseId(caseId);
            Response response = responseRepository.findByCaseId(caseId);
            page = new PDPage();
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, 700);
            contentStream.setFont(boldFont, fontSize);
            contentStream.showText("Risk Assessment");
            contentStream.setFont(normalFont, fontSize);
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("Priority: %s", riskAssessment.getPriority()));
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("From or affecting a child: %s", riskAssessment.getFromOrAffectingAChild()));

            // response
            contentStream.newLineAtOffset(0, -leading * 2);
            contentStream.setFont(boldFont, fontSize);
            contentStream.showText("Response");
            contentStream.setFont(normalFont, fontSize);
            contentStream.newLineAtOffset(0, -leading);
            contentStream.showText(String.format("QA: %s", response.getResponse()));
            contentStream.endText();
            contentStream.close();

            // case links
            List<CaseLinks> caseLinks = caseLinksRepository.findAllBySourceCaseId(caseId);
            caseLinks.addAll(caseLinksRepository.findAllByTargetCaseId(caseId));
            page = new PDPage();
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, 700);
            contentStream.setFont(boldFont, fontSize);
            contentStream.showText("Case Links");
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

            // Case history
            List<CaseHistory> caseHistoryEvents = caseHistoryRepository.findAllByCaseId(caseId);
            page = new PDPage();
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, 700);
            contentStream.setFont(boldFont, fontSize);
            contentStream.showText("Case History");
            contentStream.setFont(normalFont, fontSize);

            BaseTable caseHistoryTable = new BaseTable(680, 700, 20, 500, margin, document, page, true,
                    true);

            List<List> caseHistoryData = new ArrayList();
            caseHistoryData.add(new ArrayList<>(Arrays.asList("Type", "Description", "Created by", "Created")));
            for (CaseHistory caseHistoryEvent : caseHistoryEvents) {
                if (caseHistoryEvent.getType() == null) caseHistoryEvent.setType("");
                if (caseHistoryEvent.getDescription() == null) caseHistoryEvent.setDescription("");
                if (caseHistoryEvent.getCreatedBy() == null) caseHistoryEvent.setCreatedBy("");
                caseHistoryData.add(new ArrayList(Arrays.asList(removeInvalidChars(caseHistoryEvent.getType()),
                        removeInvalidChars(caseHistoryEvent.getDescription()),
                        removeInvalidChars(caseHistoryEvent.getCreatedBy()),
                        caseHistoryEvent.getCreated().toString())));
            }
            DataTable caseHistoryDataTable = new DataTable(caseHistoryTable, page);
            caseHistoryDataTable.addListToTable(caseHistoryData, DataTable.HASHEADER);
            caseHistoryTable.draw();
            contentStream.endText();
            contentStream.close();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            document.close();
            byte[] pdfBytes = baos.toByteArray();
            String tempFileName = documentS3Client.storeUntrustedDocument(CMS_CASE_DATA_FILENAME, pdfBytes, caseId);

            CaseAttachment caseAttachment = new CaseAttachment();
            caseAttachment.setDocumentPath(tempFileName);
            caseAttachment.setDocumentType(DocumentType.MIGRATION.getLabel());
            caseAttachment.setDisplayName(CMS_CASE_DATA_FILENAME);
            return caseAttachment;
        } catch (IOException e) {
            throw new ApplicationExceptions.CreateMigrationDocumentException(e.getMessage(),
                    LogEvent.MIGRATION_DOCUMENT_FAILED);
        }
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

    private String[] makeParagraph(String text) {
        String desc = removeInvalidChars(text);
        return WordUtils.wrap(desc, 60).split("\\r?\\n");
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
