package com.metrolog.backend.ocr.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedDeclaration {
    private String mrp;
    private String netQuantity;
    private String dateOfManufacture;
    private String manufacturerName;
    private String countryOfOrigin;
    private String consumerCareDetails;
}