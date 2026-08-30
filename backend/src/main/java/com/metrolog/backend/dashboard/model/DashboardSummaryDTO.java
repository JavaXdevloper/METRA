package com.metrolog.backend.dashboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {
    private long totalInspections;
    private long compliantInspections;
    private long nonCompliantInspections;
    private double complianceRate;
    private Map<String, Long> violationsByType;
}