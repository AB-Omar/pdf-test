package com.example.demo.controler;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.*;
import java.nio.file.FileSystems;

import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.layout.SharedContext;

@RestController
@RequestMapping(value = "")
public class testController {

    @GetMapping(value = "/test", produces = MediaType.APPLICATION_PDF_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> generatePdf() {
        try {
            File htmlFile = new File("temp/index.html");
            Document doc = Jsoup.parse(htmlFile, "UTF-8");
            doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

            ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            SharedContext sharedContext = renderer.getSharedContext();
            sharedContext.setPrint(true);
            sharedContext.setInteractive(false);

            String baseUrl = FileSystems.getDefault().getPath("temp")
                    .toUri().toURL().toString();

            renderer.setDocumentFromString(doc.html(), baseUrl);
            renderer.layout();
            renderer.createPDF(pdfOutputStream);

            pdfOutputStream.close();

            byte[] pdfBytes = pdfOutputStream.toByteArray();

            return ResponseEntity
                    .ok()
                    .contentLength(pdfBytes.length)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .badRequest()
                    .body(null);
        }
    }

    @GetMapping(value = "pdf")
    public ResponseEntity<byte[]> createPdf() throws IOException {
        PDDocument document = new PDDocument();
        for (int i = 0; i < 10; i++) {
            // Creating a new blank page
            PDPage blankPage = new PDPage();
            document.addPage(blankPage);

            PDPage page = document.getPage(i); // Access the current page
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(PDType1Font.TIMES_ROMAN, 12);

            // Begin the text block
            contentStream.beginText();

            // Setting the position for the line
            contentStream.newLineAtOffset(25, 500);

            String text = "This is the sample document and we are adding content to it. " + i;

            // Adding text in the form of a string
            contentStream.showText(text);

            // End the text block
            contentStream.endText();

            contentStream.close(); // Close the content stream for each page
        }

        // Saving the document
        document.save("temp/doc1.pdf");

        System.out.println("PDF created");

        // Closing the document
        document.close();

        return ResponseEntity
                .created(null)
                .body(null);

    }
    
    @GetMapping (value = "loadpdf")
    public ResponseEntity<byte[]> loadPdf() throws IOException {

        File oldFile = new File("temp/loadpdf.pdf");
        PDDocument document = PDDocument.load(oldFile);
        PDPage page = new PDPage(PDRectangle.A4);
        page.setRotation(90);
        document.addPage(page);
        document.save("temp/newPdf.pdf");
        System.out.println("testController.loadPdf()");
        return ResponseEntity
                .created(null)
                .body(null);
    }
}
