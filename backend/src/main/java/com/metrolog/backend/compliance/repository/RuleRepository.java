package com.metrolog.backend.compliance.repository;

import com.metrolog.backend.compliance.model.Rule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleRepository extends MongoRepository<Rule, String> {
    List<Rule> findByValidationType(String validationType);
    List<Rule> findByRequired(boolean required);
}
