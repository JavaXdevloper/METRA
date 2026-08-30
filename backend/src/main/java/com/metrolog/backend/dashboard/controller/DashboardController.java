// package com.metrolog.backend.dashboard.controller;

// import com.metrolog.backend.dashboard.model.DashboardSummaryDTO;
// import com.metrolog.backend.dashboard.service.DashboardService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// @RestController
// @RequestMapping("/api/dashboard")
// public class DashboardController {

//     @Autowired
//     private DashboardService dashboardService;

//     @GetMapping("/summary")
//     public ResponseEntity<DashboardSummaryDTO> getSummary() {
//         return ResponseEntity.ok(dashboardService.getSummary());
//     }
// }







package com.metrolog.backend.dashboard.controller;

import com.metrolog.backend.dashboard.model.DashboardSummaryDTO;
import com.metrolog.backend.dashboard.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    // Spec Endpoint: GET /api/dashboard/violations (Section 14 & 16)
    @GetMapping("/violations")
    public ResponseEntity<Map<String, Long>> getViolationCounts() {
        DashboardSummaryDTO summary = dashboardService.getSummary();
        return ResponseEntity.ok(summary.getViolationsByType());
    }
}