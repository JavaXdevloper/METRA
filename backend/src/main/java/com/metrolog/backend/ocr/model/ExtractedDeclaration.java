package com.metrolog.backend.ocr.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedDeclaration {
    // Core mandatory PC Rules 2011 fields
    private String mrp;
    private String netQuantity;
    private String dateOfManufacture;
    private String expiryDate;
    private String manufacturerName;
    private String manufacturerAddress;
    private String countryOfOrigin;
    private String consumerCareDetails;

    // Additional mandatory declarations
    private String productName;
    private String batchNumber;
    private String packerName;
    private String importerName;
    private String ingredients;
    private String fssaiLicense;
    private String barcode;
    private String unitSalePrice;
    private String bestBefore;
    private String rawMaterialOrigin;
}