package com.metrolog.backend.product.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String productName;
    private String manufacturer;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
}