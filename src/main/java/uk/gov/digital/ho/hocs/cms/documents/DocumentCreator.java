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
import uk.gov.digital.ho.hocs.cms.domain.model.Address;
import uk.gov.digital.ho.hocs.cms.domain.model.Individual;
import uk.gov.digital.ho.hocs.cms.domain.repository.IndividualRepository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

    public DocumentCreator(IndividualRepository individualRepository) {
        this.individualRepository = individualRepository;
    }

    @Transactional
    public void createDocument(BigDecimal caseId) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        File file = new File("/Users/rjweeks/hocs/hocs-cms-data-migrator/test.pdf");
        //PdfWriter.getInstance(document, new FileOutputStream(file)).setInitialLeading(16);
        PdfWriter.getInstance(document, byteArrayOutputStream).setInitialLeading(16);

        List<BigDecimal> individualIds = individualRepository.findIndividualsByCaseId(caseId);
        BigDecimal partyId = individualIds.get(0);

        Optional<Individual> ind = individualRepository.findById(partyId);
        //Address a = individual.get(0).getAddress();
        Individual ind1 = ind.get();
        Address a = ind1.getAddress();
        document.open();
        Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
        Chunk chunk = new Chunk("Personal details", font);
        document.add(chunk);

        Phrase section = new Phrase();
        section.add(new Chunk("Hello World"));
        document.add(section);

        String content = "Complainant";
        Paragraph para1 = new Paragraph(32);
        para1.setSpacingBefore(50);
        para1.setSpacingAfter(50);
        para1.add(new Chunk(content));
        para1.add(new DottedLineSeparator());
        document.add(para1);

        document.add(new DottedLineSeparator());
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEXTPAGE);
        document.add(new LineSeparator());

        String rep = "Representative";
        Paragraph para2 = new Paragraph(32);
        para2.setSpacingBefore(50);
        para2.setSpacingAfter(50);
        para2.add(new Chunk(rep));
        document.add(para2);



        PdfPTable table = new PdfPTable(3);
        addTableHeader(table);
        addRows(table);

        document.add(table);
        document.close();
        byte[] pdfBytes = byteArrayOutputStream.toByteArray();
        Files.write(file.toPath(), pdfBytes);
    }

    public Paragraph createNewParagraph(String title) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(title));
        p.add(Chunk.NEWLINE);
        return p;
    }


    private void addTableHeader(PdfPTable table) {
        Stream.of("column header 1", "column header 2", "column header 3")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    table.addCell(header);
                });
    }

    private void addRows(PdfPTable table) {
        table.addCell("row 1, col 1");
        table.addCell("row 1, col 2");
        table.addCell("row 1, col 3");
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }


    public static void main(String[] args) throws DocumentException, IOException {
        //DocumentCreator dc = new DocumentCreator();
        //dc.createDocument();
    }
}
