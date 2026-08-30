package com.metrolog.backend.dashboard.service;

import com.metrolog.backend.dashboard.model.DashboardSummaryDTO;
import com.metrolog.backend.inspection.model.Inspection;
import com.metrolog.backend.inspection.model.Violation;
import com.metrolog.backend.inspection.repository.InspectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private InspectionRepository inspectionRepository;

    public DashboardSummaryDTO getSummary() {
        List<Inspection> inspections = inspectionRepository.findAll();

        long total = inspections.size();
        long compliant = inspections.stream()
                .filter(i -> "COMPLIANT".equalsIgnoreCase(i.getComplianceStatus()))
                .count();
        long nonCompliant = total - compliant;
        double complianceRate = total > 0 ? ((double) compliant / total) * 100.0 : 0.0;

        Map<String, Long> violationsByType = new HashMap<>();
        for (Inspection inspection : inspections) {
            if (inspection.getViolations() != null) {
                for (Violation v : inspection.getViolations()) {
                    String type = v.getType() != null ? v.getType() : "UNKNOWN";
                    violationsByType.put(type, violationsByType.getOrDefault(type, 0L) + 1);
                }
            }
        }

        return new DashboardSummaryDTO(total, compliant, nonCompliant, complianceRate, violationsByType);
    }
}