package com.metrolog.backend.compliance.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "rules")
public class Rule {
    @Id
    private String id;
    private String ruleReference;
    private String declaration;
    private String description;
    private String validationType;
    private boolean required;
}