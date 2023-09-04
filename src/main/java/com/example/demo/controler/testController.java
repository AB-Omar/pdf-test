package com.example.demo.controler;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
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

            // Save the byte[] to a file in temp directory
            try (FileOutputStream fos = new FileOutputStream("temp/docccc.pdf")) {
                fos.write(pdfBytes);
            }
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

    @GetMapping(value = "loadpdf")
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

    @GetMapping(value = "mergepdf")
    public ResponseEntity<byte[]> mergepdf() throws IOException {

        File oldFile = new File("temp/loadpdf.pdf");
        File oldFile1 = new File("temp/output.pdf");

        PDDocument document = PDDocument.load(oldFile);
        PDDocument document1 = PDDocument.load(oldFile1);

        for (int i = 0; i < document1.getNumberOfPages(); i++) {
            document.addPage(document1.getPage(i));
        }
        document.save("temp/newPdf.pdf");
        System.out.println("testController.loadPdf()");
        return ResponseEntity
                .created(null)
                .body(null);
    }

    @GetMapping(value = "addimage")
    public ResponseEntity<byte[]> addImage(
        @RequestParam("file") MultipartFile file,
        @RequestParam("percentage") Integer percentage
    ) throws IOException {
        
        final String headerText = "This is the header";
        final String footerText = "This is the footer";
        File oldFile = new File("temp/loadpdf.pdf");
        PDDocument document = PDDocument.load(oldFile);
    
        int lastPageIndex = document.getNumberOfPages() - 1;
        PDPage lastPage = document.getPage(lastPageIndex);
    
        PDImageXObject image = PDImageXObject.createFromByteArray(document, file.getBytes(), "added-image");
    
        float scale = (float) percentage / 100;
        float newWidth = image.getWidth() * scale;
        float newHeight = image.getHeight() * scale;
    
        float pageHeight = lastPage.getMediaBox().getHeight();
        float pageWidth = lastPage.getMediaBox().getWidth();
        
        float yPosition = pageHeight - newHeight - 50; // start 50 units from the top
        float xPosition = 50; // start 50 units from the left
    
        // Loop to add the image three times
        for (int i = 0; i < 3; i++) {
            // Check if the image fits vertically in the remaining space
            if (yPosition - newHeight < 0) {
                // Add a new page and reset positions
                lastPage = new PDPage();
                document.addPage(lastPage);
                yPosition = pageHeight - newHeight - 50;
                xPosition = 50;
            }
    
            try (PDPageContentStream contentStream = new PDPageContentStream(document, lastPage, PDPageContentStream.AppendMode.APPEND, true, true)) {
                contentStream.drawImage(image, xPosition, yPosition, newWidth, newHeight);
            }
    
            yPosition -= (newHeight + 50);  // Move down for the next image, leaving 50 units as a gap
        }
    
        addHeadersAndFooters(document);
        document.save("temp/newPdf.pdf");
    
        return ResponseEntity
                .created(null)
                .body(null);
    }
    
    public void addHeadersAndFooters(PDDocument document) throws IOException {
        final String headerText = "This is the header";
        final String footerText = "This is the footer";
    
        for (int i = 0; i < document.getNumberOfPages(); ++i) {
            PDPage page = document.getPage(i);
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
    
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                // Header
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, pageHeight - 50);  // position 50 units from top and left
                contentStream.showText(headerText);
                contentStream.endText();
    
                // Footer
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 50);  // position 50 units from bottom and left
                contentStream.showText(footerText);
                contentStream.endText();
            }
        }
    }
    
}
