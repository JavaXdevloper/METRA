package com.metrolog.backend.report.controller;

import com.metrolog.backend.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // Text Report Endpoints
    @GetMapping("/reports/inspection/{inspectionId}/download")
    public ResponseEntity<byte[]> downloadReport(@PathVariable String inspectionId) {
        String reportContent = reportService.generateTextReport(inspectionId);
        byte[] bytes = reportContent.getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "inspection_report_" + inspectionId + ".txt");

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }

    @GetMapping("/reports/inspection/{inspectionId}")
    public ResponseEntity<String> getReportText(@PathVariable String inspectionId) {
        String reportContent = reportService.generateTextReport(inspectionId);
        return ResponseEntity.ok(reportContent);
    }

    // PDF Report Endpoints (Required by Backend Spec Section 15 & 16)
    @GetMapping("/inspections/{id}/report/pdf")
    public ResponseEntity<byte[]> getPdfReport(@PathVariable("id") String id) throws Exception {
        byte[] pdfBytes = reportService.generatePdfReport(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "inspection_report_" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/reports/inspection/{inspectionId}/pdf")
    public ResponseEntity<byte[]> downloadPdfReportAlias(@PathVariable("inspectionId") String inspectionId)
            throws Exception {
        return getPdfReport(inspectionId);
    }
}