package com.metrolog.backend.inspection.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Violation {
    private String type;
    private String description;
    private String status; // NON_COMPLIANT
    private String ruleReference;
    private String evidenceImage;
}