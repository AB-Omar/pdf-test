package com.example.demo.controler;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
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
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping (value = "")
public class testController {

    @Autowired
    private ResourceLoader resourceLoader;

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
    @GetMapping(value="pdf")
    public ResponseEntity<byte[]> createPdf() throws IOException {

        //Creating PDF document object 
      PDDocument document = new PDDocument();  
      for (int i=0; i<10; i++) {
        //Creating a blank page 
        PDPage blankPage = new PDPage();

        //Adding the blank page to the document
        document.addPage( blankPage );
     } 
       
      //Saving the document
      document.save("temp/doc1.pdf");
         
      System.out.println("PDF created");  
    
      //Closing the document  
      document.close();
         return ResponseEntity
                    .created(null)
                    .body(null);

    } 
}
