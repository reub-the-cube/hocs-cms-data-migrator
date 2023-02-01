package uk.gov.digital.ho.hocs.cms.documents;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class DocumentCreator {

    public void createDocument()  {
        Document document = new Document();
        try {
            File file = new File("/Users/rjweeks/hocs/hocs-cms-data-migrator/test.pdf");
            PdfWriter.getInstance(document, new FileOutputStream(file));
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        document.open();
        Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
        Chunk chunk = new Chunk("Hello World", font);

        try {
            document.add(chunk);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
        document.close();
    }

    public static void main(String[] args) {
        DocumentCreator dc = new DocumentCreator();
        dc.createDocument();
    }
}
