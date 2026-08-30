package com.metrolog.backend.ocr.service;

import com.metrolog.backend.ocr.model.OcrResult;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class OcrService {

    @Value("${tessdata.path:tessdata}")
    private String tessdataPath;

    public OcrResult extractTextFromImage(MultipartFile file) throws IOException, TesseractException {
        File convFile = convertMultipartToFile(file);

        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath(tessdataPath);
        tesseract.setLanguage("eng");

        try {
            String result = tesseract.doOCR(convFile);
            return new OcrResult(result.trim(), 100.0f);
        } finally {
            if (convFile.exists()) {
                convFile.delete();
            }
        }
    }

    private File convertMultipartToFile(MultipartFile file) throws IOException {
        File convFile = File.createTempFile("ocr_", "_" + file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
}