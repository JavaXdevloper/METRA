package com.metrolog.backend.inspection.repository;

import com.metrolog.backend.inspection.model.Inspection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InspectionRepository extends MongoRepository<Inspection, String> {
    List<Inspection> findByProductId(String productId);

    List<Inspection> findByUserId(String userId);
}