package com.metrolog.backend.inspection.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "inspections")
public class Inspection {
    @Id
    private String id;
    private String productId;
    private String userId;
    private List<String> images;
    private Map<String, Object> ocrData;
    private Map<String, String> extractedDeclarations;
    private String complianceStatus; // COMPLIANT, NON_COMPLIANT, NEEDS_REVIEW
    private List<Violation> violations;
    private Instant createdAt = Instant.now();
}