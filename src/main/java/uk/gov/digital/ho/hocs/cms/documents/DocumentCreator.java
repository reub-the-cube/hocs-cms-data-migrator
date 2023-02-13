package uk.gov.digital.ho.hocs.cms.documents;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import com.itextpdf.text.pdf.draw.LineSeparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.cms.correspondents.CorrespondentType;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseData;
import uk.gov.digital.ho.hocs.cms.domain.model.Individual;
import uk.gov.digital.ho.hocs.cms.domain.model.Reference;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.cms.domain.repository.IndividualRepository;

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

    public DocumentCreator(IndividualRepository individualRepository,
                           CaseDataRepository caseDataRepository) {
        this.individualRepository = individualRepository;
        this.caseDataRepository = caseDataRepository;
    }

    @Transactional
    public void createDocument(BigDecimal caseId) throws IOException, DocumentException {
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
        Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);

        Phrase section = new Phrase();
        section.add(new Chunk("Personal Details"));
        document.add(section);
        document.add(Chunk.NEWLINE);
        document.add(new Chunk(String.format("Complainant: %s", complainant.getPartyId())));
        document.add(Chunk.NEWLINE);
        Paragraph complainantDetails = createCorrespondentSection(complainant);
        document.add(complainantDetails);
        document.add(Chunk.NEWLINE);
        document.add(createReferencesSection(complainant));

        document.add(new DottedLineSeparator());
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEXTPAGE);
        document.add(new LineSeparator());



        document.add(new Chunk(String.format("Representative: %s", representative.getPartyId())));

        Paragraph representativeSection = createCorrespondentSection(representative);
        document.add(representativeSection);

        document.add(createReferencesSection(representative));

        document.add(Chunk.NEXTPAGE);
        CaseData casedata = caseDataRepository.findByCaseId(caseId);

        document.add(addLine("Case Data"));
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

        document.close();
        byte[] pdfBytes = byteArrayOutputStream.toByteArray();
        Files.write(file.toPath(), pdfBytes);
    }

    private Paragraph createCorrespondentSection(Individual individual) {
        Paragraph complainantDetails = new Paragraph(32);
        complainantDetails.setSpacingBefore(10);
        complainantDetails.setSpacingAfter(10);
        complainantDetails.add(Chunk.NEWLINE);
        complainantDetails.add(addLine(String.format("Forename: %s", individual.getForename())));
        complainantDetails.add(addLine(String.format("Surname: %s", individual.getSurname())));
        complainantDetails.add(addLine(String.format("Date of birth: %s", individual.getDateOfBirth())));
        complainantDetails.add(addLine(String.format("Nationality: %s", individual.getNationality())));
        complainantDetails.add(addLine(String.format("Telephone: %s", individual.getTelephone())));
        complainantDetails.add(addLine(String.format("Email: %s", individual.getEmail())));
        complainantDetails.add(new DottedLineSeparator());
        complainantDetails.add(Chunk.NEWLINE);
        complainantDetails.add(new Chunk("Address"));
        complainantDetails.add(Chunk.NEWLINE);
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
        Paragraph p = new Paragraph();
        p.add(new Chunk("References"));
        p.add(Chunk.NEWLINE);
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
        p.add(table);
        return p;
    }

    public Paragraph createNewParagraph(String title) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(title));
        p.add(Chunk.NEWLINE);
        return p;
    }

    private Phrase addLine(String s) {
        Phrase p =new Phrase(s);
        p.add(Chunk.NEWLINE);
        return p;
    }

}
