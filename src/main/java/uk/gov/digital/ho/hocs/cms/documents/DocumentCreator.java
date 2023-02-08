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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

@Component
@Slf4j
public class DocumentCreator {

    public void createDocument() throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        File file = new File("/Users/rjweeks/hocs/hocs-cms-data-migrator/test.pdf");
        //PdfWriter.getInstance(document, new FileOutputStream(file)).setInitialLeading(16);
        PdfWriter.getInstance(document, byteArrayOutputStream).setInitialLeading(16);


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


    public static void main(String[] args) throws DocumentException, IOException {
        DocumentCreator dc = new DocumentCreator();
        dc.createDocument();
    }
}
