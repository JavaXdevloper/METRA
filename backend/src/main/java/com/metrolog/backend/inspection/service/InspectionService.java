package com.metrolog.backend.inspection.service;

import com.metrolog.backend.compliance.service.ComplianceService;
import com.metrolog.backend.inspection.model.Inspection;
import com.metrolog.backend.inspection.model.Violation;
import com.metrolog.backend.inspection.repository.InspectionRepository;
import com.metrolog.backend.ocr.model.ExtractedDeclaration;
import com.metrolog.backend.ocr.model.OcrResult;
import com.metrolog.backend.ocr.service.DeclarationExtractorService;
import com.metrolog.backend.ocr.service.OcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InspectionService {

    @Autowired
    private OcrService ocrService;

    @Autowired
    private DeclarationExtractorService declarationExtractorService;

    @Autowired
    private ComplianceService complianceService;

    @Autowired
    private InspectionRepository inspectionRepository;

    // Single-image process (Maintained for existing single file uploads)
    public Inspection processInspection(String productId, String userId, MultipartFile imageFile) throws Exception {
        List<MultipartFile> files = new ArrayList<>();
        if (imageFile != null) {
            files.add(imageFile);
        }
        return processMultipleInspections(productId, userId, files);
    }

    // Multi-image process (Fulfills Section 5 of Backend Spec)
    public Inspection processMultipleInspections(String productId, String userId, List<MultipartFile> imageFiles)
            throws Exception {
        StringBuilder combinedText = new StringBuilder();
        double totalConfidence = 0.0;
        List<String> imageNames = new ArrayList<>();

        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                OcrResult result = ocrService.extractTextFromImage(file);
                combinedText.append(result.getExtractedText()).append("\n");
                totalConfidence += result.getConfidence();
                imageNames.add(file.getOriginalFilename());
            }
        }

        double averageConfidence = (imageFiles != null && !imageFiles.isEmpty()) ? (totalConfidence / imageFiles.size())
                : 0.0;

        ExtractedDeclaration extractedDec = declarationExtractorService.extractDeclarations(combinedText.toString());
        List<Violation> violations = complianceService.evaluateDeclarations(extractedDec);

        Inspection inspection = new Inspection();
        inspection.setProductId(productId);
        inspection.setUserId(userId);
        inspection.setImages(imageNames);

        Map<String, Object> ocrDataMap = new HashMap<>();
        ocrDataMap.put("extractedText", combinedText.toString());
        ocrDataMap.put("confidence", averageConfidence);
        inspection.setOcrData(ocrDataMap);

        Map<String, String> decMap = new HashMap<>();
        decMap.put("mrp", extractedDec.getMrp());
        decMap.put("netQuantity", extractedDec.getNetQuantity());
        decMap.put("dateOfManufacture", extractedDec.getDateOfManufacture());
        decMap.put("expiryDate", extractedDec.getExpiryDate());
        decMap.put("manufacturerName", extractedDec.getManufacturerName());
        decMap.put("manufacturerAddress", extractedDec.getManufacturerAddress());
        decMap.put("packerName", extractedDec.getPackerName());
        decMap.put("importerName", extractedDec.getImporterName());
        decMap.put("countryOfOrigin", extractedDec.getCountryOfOrigin());
        decMap.put("consumerCareDetails", extractedDec.getConsumerCareDetails());
        decMap.put("productName", extractedDec.getProductName());
        decMap.put("batchNumber", extractedDec.getBatchNumber());
        decMap.put("ingredients", extractedDec.getIngredients());
        decMap.put("fssaiLicense", extractedDec.getFssaiLicense());
        decMap.put("barcode", extractedDec.getBarcode());
        decMap.put("unitSalePrice", extractedDec.getUnitSalePrice());
        decMap.put("bestBefore", extractedDec.getBestBefore());
        decMap.put("rawMaterialOrigin", extractedDec.getRawMaterialOrigin());
        inspection.setExtractedDeclarations(decMap);

        inspection.setViolations(violations);

        String status = complianceService.determineComplianceStatus(violations);
        inspection.setComplianceStatus(status);

        // FIXED: Pass Instant directly instead of String
        inspection.setCreatedAt(Instant.now());

        return inspectionRepository.save(inspection);
    }

    public List<Inspection> getAllInspections() {
        return inspectionRepository.findAll();
    }

    public Inspection getInspectionById(String id) {
        return inspectionRepository.findById(id).orElse(null);
    }
}