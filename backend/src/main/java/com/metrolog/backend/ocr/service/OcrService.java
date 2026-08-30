package com.metrolog.backend.ocr.service;

import com.metrolog.backend.ocr.model.OcrResult;
import jakarta.annotation.PostConstruct;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrService.class);
    private Path tempTessDataDir;

    @PostConstruct
    public void init() {
        try {
            tempTessDataDir = Files.createTempDirectory("metra_tessdata");
            File trainedDataFile = tempTessDataDir.resolve("eng.traineddata").toFile();

            ClassPathResource resource = new ClassPathResource("tessdata/eng.traineddata");
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    Files.copy(is, trainedDataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    log.info("Loaded eng.traineddata to temporary datapath: {}", tempTessDataDir.toAbsolutePath());
                }
            } else {
                log.warn("tessdata/eng.traineddata not found in classpath.");
            }
        } catch (Exception e) {
            log.error("Failed to initialize tessdata directory: {}", e.getMessage(), e);
        }
    }

    public OcrResult extractTextFromImage(MultipartFile file) throws IOException {
        File convFile = convertMultipartToFile(file);

        try {
            if (tempTessDataDir == null || !Files.exists(tempTessDataDir.resolve("eng.traineddata"))) {
                log.warn("Tessdata not ready. Returning empty OCR extraction.");
                return new OcrResult("", 0.0f);
            }

            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath(tempTessDataDir.toAbsolutePath().toString());
            tesseract.setLanguage("eng");

            String result = tesseract.doOCR(convFile);
            return new OcrResult(result != null ? result.trim() : "", 95.0f);
        } catch (Throwable t) {
            log.error("Error during OCR extraction: {}", t.getMessage());
            return new OcrResult("", 0.0f);
        } finally {
            if (convFile.exists()) {
                convFile.delete();
            }
        }
    }

    private File convertMultipartToFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String suffix = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : ".tmp";
        File convFile = File.createTempFile("ocr_", suffix);
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }
}