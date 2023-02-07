package uk.gov.digital.ho.hocs.cms.documents;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.stream.Stream;

public class DocumentCreator {

    public void createDocument() throws FileNotFoundException, DocumentException {
        Document document = new Document();
        File file = new File("/Users/rjweeks/hocs/hocs-cms-data-migrator/test.pdf");
        PdfWriter.getInstance(document, new FileOutputStream(file));


        document.open();
        Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
        Chunk chunk = new Chunk("Hello World", font);

        document.add(chunk);

        String content = "The quick brown fox jumps over the lazy dog";
        Paragraph para1 = new Paragraph(32);
        para1.setSpacingBefore(50);
        para1.setSpacingAfter(50);
        for (int i = 0; i < 10; i++) {
            para1.add(new Chunk(content));
        }
        document.add(para1);

        PdfPTable table = new PdfPTable(3);
        addTableHeader(table);
        addRows(table);

        document.add(table);
        document.close();
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


    public static void main(String[] args) throws DocumentException, FileNotFoundException {
        DocumentCreator dc = new DocumentCreator();
        dc.createDocument();
    }
}
