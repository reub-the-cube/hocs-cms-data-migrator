package uk.gov.digital.ho.hocs.cms.documents;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.cms.client.DocumentS3Client;
import uk.gov.digital.ho.hocs.cms.correspondents.CorrespondentType;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseData;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseHistory;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseLinks;
import uk.gov.digital.ho.hocs.cms.domain.model.Categories;
import uk.gov.digital.ho.hocs.cms.domain.model.Compensation;
import uk.gov.digital.ho.hocs.cms.domain.model.Individual;
import uk.gov.digital.ho.hocs.cms.domain.model.Reference;
import uk.gov.digital.ho.hocs.cms.domain.model.Response;
import uk.gov.digital.ho.hocs.cms.domain.model.RiskAssessment;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseHistoryRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseLinksRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


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

    @Transactional
    public String createDocument(BigDecimal caseId) throws DocumentException{
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        File file = new File("/Users/rjweeks/hocs/hocs-cms-data-migrator/test.pdf");
        PdfWriter.getInstance(document, byteArrayOutputStream).setInitialLeading(16);

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

        document.open();
        document.add(addTitle("Personal Details"));
        document.add(addTitle(String.format("Complainant: %s", complainant.getPartyId())));
        document.add(createCorrespondentSection(complainant));
        document.add(Chunk.NEWLINE);
        document.add(createReferencesSection(complainant));

        if (representative != null) {
            document.add(Chunk.NEXTPAGE);
            document.add(addTitle(String.format("Representative: %s", representative.getPartyId())));
            Paragraph representativeSection = createCorrespondentSection(representative);
            document.add(representativeSection);
            document.add(createReferencesSection(representative));
        }

        // case data
        document.add(Chunk.NEXTPAGE);
        CaseData casedata = caseDataRepository.findByCaseId(caseId);
        document.add(addTitle("Case Data"));
        document.add(addLine(String.format("Reference: %s", casedata.getCaseReference())));
        document.add(addLine(String.format("Date Received: %s", casedata.getReceiveDate())));
        document.add(addLine(String.format("Due Date: %s", casedata.getSlaDate())));
        document.add(addLine(String.format("Initial Type: %s", casedata.getInitialType())));
        document.add(addLine(String.format("Current Type: %s", casedata.getCurrentType())));
        document.add(addLine(String.format("Description: %s", casedata.getDescription())));
        document.add(addLine(String.format("Current Work Queue: %s", casedata.getQueueName())));
        document.add(addLine(String.format("Location: %s", casedata.getLocation())));
        document.add(addLine(String.format("NRO: %s", casedata.getNroCombo())));
        document.add(addLine(String.format("Closed Date: %s", casedata.getClosedDt())));
        document.add(addLine(String.format("Owning CSU: %s", casedata.getOwningCsu())));
        document.add(addLine(String.format("Business Area: %s", casedata.getBusinessArea())));
        document.add(addLine(String.format("Status: %s", casedata.getStatus())));

        // compensation
        Compensation compensation = compensationRepository.findByCaseId(caseId);
        document.add(Chunk.NEXTPAGE);
        document.add(addTitle("Compensation"));
        document.add(addLine(String.format("Date of Compensation Claim: %s", compensation.getDateOfCompensationClaim())));
        document.add(addLine(String.format("Offer Accepted: %s", compensation.getOfferAccepted())));
        document.add(addLine(String.format("Date of Payment", compensation.getDateOfPayment())));
        document.add(addLine(String.format("Compensation Amount: %s", compensation.getCompensationAmmount())));
        document.add(addLine(String.format("Amount Claimed: %s", compensation.getAmountClaimed())));
        document.add(addLine(String.format("Amount Offered: %s", compensation.getAmountOffered())));
        document.add(addLine(String.format("Consolatory Payment: %s", compensation.getConsolatoryPayment())));

        // complaint category breakdown
        document.add(Chunk.NEXTPAGE);
        document.add(addTitle("Complaint Category Breakdown"));
        document.add(createCategoriesSection(caseId));;

        // risk assessment
        RiskAssessment riskAssessment = riskAssessmentRepository.findByCaseId(caseId);

        document.add(Chunk.NEXTPAGE);
        document.add(addTitle("Risk Assessment"));
        document.add(addLine(String.format("Priority: %s", riskAssessment.getPriority())));
        document.add(addLine(String.format("From or affecting a child: %s", riskAssessment.getFromOrAffectingAChild())));

        // response
        Response response = responseRepository.findByCaseId(caseId);
        document.add(addLine(String.format("QA: %s", response.getResponse())));

        // case links
        document.add(Chunk.NEXTPAGE);
        document.add(createCaseLinksSection(caseId));

        // case history
        document.add(Chunk.NEXTPAGE);
        document.add(addTitle("Case History"));
        document.add(createCaseHistorySection(caseId));

        document.close();

        byte[] pdfBytes = byteArrayOutputStream.toByteArray();
        String tempFileName = documentS3Client.storeUntrustedDocument("CMS_CASE_DATA", pdfBytes, caseId);
        try {
            Files.write(file.toPath(), pdfBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tempFileName;
    }

    private Paragraph createCorrespondentSection(Individual individual) {
        Paragraph complainantDetails = new Paragraph(32);
        complainantDetails.setSpacingBefore(10);
        complainantDetails.setSpacingAfter(10);
        complainantDetails.add(addLine(String.format("Forename: %s", individual.getForename())));
        complainantDetails.add(addLine(String.format("Surname: %s", individual.getSurname())));
        complainantDetails.add(addLine(String.format("Date of birth: %s", individual.getDateOfBirth())));
        complainantDetails.add(addLine(String.format("Nationality: %s", individual.getNationality())));
        complainantDetails.add(addLine(String.format("Telephone: %s", individual.getTelephone())));
        complainantDetails.add(addLine(String.format("Email: %s", individual.getEmail())));
        complainantDetails.add(addTitle("Address"));
        complainantDetails.add(addLine(String.format("House name/number: %s", individual.getAddress().getNumber())));
        complainantDetails.add(addLine(String.format("Address line 1: %s", individual.getAddress().getAddressLine1())));
        complainantDetails.add(addLine(String.format("Address Line 2: %s", individual.getAddress().getAddressLine2())));
        complainantDetails.add(addLine(String.format("Address Line 3: %s", individual.getAddress().getAddressLine3())));
        complainantDetails.add(addLine(String.format("Address Line 4: %s", individual.getAddress().getAddressLine4())));
        complainantDetails.add(addLine(String.format("Address Line 5: %s", individual.getAddress().getAddressLine5())));
        complainantDetails.add(addLine(String.format("Address Line 6: %s", individual.getAddress().getAddressLine6())));
        complainantDetails.add(addLine(String.format("Postcode: %s", individual.getAddress().getPostcode())));
        return complainantDetails;
    }

    private Paragraph createReferencesSection(Individual individual) {
        Paragraph paragraph = new Paragraph();
        paragraph.add(addTitle("References"));
        PdfPTable table = new PdfPTable(2);
        Stream.of("Reference Type", "Reference")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    table.addCell(header);
                });
        List<Reference> refs = individual.getReferences();
        for (Reference ref : refs) {
            table.addCell(ref.getRefType());
            table.addCell(ref.getReference());
        }
        paragraph.add(table);
        return paragraph;
    }

    private Paragraph createCaseLinksSection(BigDecimal caseId) {
        Paragraph paragraph = new Paragraph();
        PdfPTable table = new PdfPTable(3);
        Stream.of("Source Case", "Link Type", "Target Type")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    table.addCell(header);
                });
        List<CaseLinks> caseLinks = caseLinksRepository.findAllBySourceCaseId(caseId);
        caseLinks.addAll(caseLinksRepository.findAllByTargetCaseId(caseId));
        for (CaseLinks caseLink : caseLinks) {
            table.addCell(caseLink.getSourceCaseId().toString());
            table.addCell(caseLink.getDescription());
            table.addCell(caseLink.getTargetCaseId().toString());
        }
        paragraph.add(table);
        return paragraph;
    }

    private Paragraph createCategoriesSection(BigDecimal caseId) {
            Paragraph paragraph = new Paragraph();
            PdfPTable table = new PdfPTable(4);
            Stream.of("Category", "Selected", "Substantiated", "Amount")
                    .forEach(columnTitle -> {
                        PdfPCell header = new PdfPCell();
                        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        header.setBorderWidth(2);
                        header.setPhrase(new Phrase(columnTitle));
                        table.addCell(header);
                    });
            List<Categories> categories = categoriesRepository.findByCaseId(caseId);
            for (Categories category : categories) {
                table.addCell(category.getCategory());
                table.addCell(category.getSelected());
                table.addCell(category.getSubstantiated());
                table.addCell(category.getAmount().toString());
            }
            paragraph.add(table);
            return paragraph;
        }

    private Paragraph createCaseHistorySection(BigDecimal caseId) {
        Paragraph paragraph = new Paragraph();
        PdfPTable table = new PdfPTable(4);
        Stream.of("Type", "Description", "Created By", "Created")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    table.addCell(header);
                });
        List<CaseHistory> caseHistoryEvents = caseHistoryRepository.findAllByCaseId(caseId);
        for (CaseHistory caseHistoryEvent : caseHistoryEvents) {
            table.addCell(caseHistoryEvent.getType());
            table.addCell(caseHistoryEvent.getDescription());
            table.addCell(caseHistoryEvent.getCreatedBy());
            table.addCell(caseHistoryEvent.getCreated().toString());
        }
        paragraph.add(table);
        return paragraph;
    }

    private Phrase addLine(String s) {
        Font regular = new Font(Font.FontFamily.HELVETICA, 12);
        Paragraph p =new Paragraph(s, regular);
        p.setSpacingBefore(5);
        p.setSpacingAfter(5);
        p.add(Chunk.NEWLINE);
        return p;
    }

    private Paragraph addTitle(String s) {
        Font bold = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Paragraph p = new Paragraph(s, bold);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingBefore(5);
        p.setSpacingAfter(10);
        p.add(Chunk.NEWLINE);
        return p;
    }

}
