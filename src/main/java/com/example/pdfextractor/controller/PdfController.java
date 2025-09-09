package com.example.pdfextractor.controller;

import com.example.pdfextractor.dto.PageRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api")
public class PdfController {

    @PostMapping(value = "/extraer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> extraerVariasPaginas(
            @RequestPart("file") MultipartFile file,
            @RequestPart("json") String json
    ) throws IOException {

        // Convertir el JSON en lista de PageRequest
        ObjectMapper mapper = new ObjectMapper();
        List<PageRequest> paginas = mapper.readValue(json, new TypeReference<List<PageRequest>>() {});

        // Cargar el documento original
        PDDocument documento = PDDocument.load(file.getInputStream());

        // Crear un buffer para el ZIP
        ByteArrayOutputStream zipBaos = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(zipBaos);

        for (PageRequest request : paginas) {
            String nombre = request.getNameDoc() + ".pdf";
            String rango = request.getPag();

            // Crear un nuevo documento para estas páginas
            PDDocument nuevoDoc = new PDDocument();

            for (String parte : rango.split(",")) {
                if (parte.contains("-")) {
                    String[] limites = parte.split("-");
                    int inicio = Integer.parseInt(limites[0]);
                    int fin = Integer.parseInt(limites[1]);
                    for (int i = inicio; i <= fin; i++) {
                        int index = i - 1; // PDFBox es 0-based
                        if (index >= 0 && index < documento.getNumberOfPages()) {
                            PDPage page = documento.getPage(index);
                            nuevoDoc.addPage(page);
                        }
                    }
                } else {
                    int pagina = Integer.parseInt(parte);
                    int index = pagina - 1;
                    if (index >= 0 && index < documento.getNumberOfPages()) {
                        PDPage page = documento.getPage(index);
                        nuevoDoc.addPage(page);
                    }
                }
            }

            // Guardar el documento en el ZIP
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            nuevoDoc.save(baos);
            nuevoDoc.close();

            ZipEntry entry = new ZipEntry(nombre);
            zipOut.putNextEntry(entry);
            zipOut.write(baos.toByteArray());
            zipOut.closeEntry();
        }

        documento.close();
        zipOut.close();

        // Devolver el ZIP como respuesta
        byte[] zipBytes = zipBaos.toByteArray();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resultados.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zipBytes);
    }
}