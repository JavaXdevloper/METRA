package com.metrolog.backend.report.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.metrolog.backend.inspection.model.Inspection;
import com.metrolog.backend.inspection.model.Violation;
import com.metrolog.backend.inspection.repository.InspectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private InspectionRepository inspectionRepository;

    public String generateTextReport(String inspectionId) {
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new IllegalArgumentException("Inspection not found with ID: " + inspectionId));

        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("     LEGAL METROLOGY COMPLIANCE INSPECTION REPORT    \n");
        sb.append("====================================================\n\n");

        sb.append("Inspection ID     : ").append(inspection.getId()).append("\n");
        sb.append("Product ID        : ").append(inspection.getProductId()).append("\n");
        sb.append("Inspector User ID : ").append(inspection.getUserId()).append("\n");
        sb.append("Created At        : ").append(inspection.getCreatedAt()).append("\n");
        sb.append("Compliance Status : ").append(inspection.getComplianceStatus()).append("\n\n");

        sb.append("--- EXTRACTED DECLARATIONS ---\n");
        if (inspection.getExtractedDeclarations() != null) {
            inspection.getExtractedDeclarations().forEach((key, value) -> sb
                    .append(String.format("%-22s : %s\n", key, value != null ? value : "NOT DETECTED")));
        } else {
            sb.append("No extracted declarations found.\n");
        }
        sb.append("\n");

        sb.append("--- VIOLATIONS / NON-COMPLIANCE FINDINGS ---\n");
        List<Violation> violations = inspection.getViolations();
        if (violations != null && !violations.isEmpty()) {
            for (int i = 0; i < violations.size(); i++) {
                Violation v = violations.get(i);
                sb.append(String.format("%d. [%s] Rule: %s | Description: %s\n",
                        i + 1,
                        v.getType() != null ? v.getType() : "VIOLATION",
                        v.getRuleReference() != null ? v.getRuleReference() : "N/A",
                        v.getDescription()));
            }
        } else {
            sb.append("No violations detected. Product meets Legal Metrology standards.\n");
        }

        sb.append("\n====================================================\n");
        sb.append("                  END OF REPORT                     \n");
        sb.append("====================================================\n");

        return sb.toString();
    }

    public byte[] generatePdfReport(String inspectionId) throws Exception {
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new IllegalArgumentException("Inspection not found with ID: " + inspectionId));

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD, BaseColor.BLUE);
        Paragraph title = new Paragraph("Legal Metrology Compliance Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        document.add(new Paragraph("Inspection ID: " + inspection.getId()));
        document.add(new Paragraph("Product ID: " + inspection.getProductId()));
        document.add(new Paragraph("Inspector User ID: " + inspection.getUserId()));
        document.add(new Paragraph("Compliance Status: " + inspection.getComplianceStatus()));
        document.add(new Paragraph("Date: " + inspection.getCreatedAt()));
        document.add(Chunk.NEWLINE);

        Paragraph tableTitle = new Paragraph("Extracted Declarations",
                FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD));
        document.add(tableTitle);
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.addCell("Field");
        table.addCell("Extracted Value");

        if (inspection.getExtractedDeclarations() != null) {
            inspection.getExtractedDeclarations().forEach((key, val) -> {
                table.addCell(key);
                table.addCell(val != null ? val : "NOT DETECTED");
            });
        }
        document.add(table);
        document.add(Chunk.NEWLINE);

        Paragraph violationTitle = new Paragraph("Violations", FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD));
        document.add(violationTitle);
        document.add(Chunk.NEWLINE);

        List<Violation> violations = inspection.getViolations();
        if (violations != null && !violations.isEmpty()) {
            for (Violation v : violations) {
                document.add(new Paragraph("• [" + (v.getType() != null ? v.getType() : "VIOLATION") + "] Rule: "
                        + (v.getRuleReference() != null ? v.getRuleReference() : "N/A") + " - " + v.getDescription()));
            }
        } else {
            document.add(new Paragraph("No violations detected. Product meets Legal Metrology standards."));
        }

        document.close();
        return out.toByteArray();
    }
}