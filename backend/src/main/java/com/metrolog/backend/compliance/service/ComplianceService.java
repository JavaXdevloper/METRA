// package com.metrolog.backend.compliance.service;

// import com.metrolog.backend.inspection.model.Violation;
// import com.metrolog.backend.ocr.model.ExtractedDeclaration;
// import org.springframework.stereotype.Service;

// import java.util.ArrayList;
// import java.util.List;

// @Service
// public class ComplianceService {

//     public List<Violation> evaluateDeclarations(ExtractedDeclaration declaration) {
//         List<Violation> violations = new ArrayList<>();

//         if (declaration == null) {
//             violations.add(new Violation("MISSING_DECLARATION", "No declaration data extracted from OCR",
//                     "NON_COMPLIANT", "RULE_00", null));
//             return violations;
//         }

//         // Rule 1: MRP must be present
//         if (declaration.getMrp() == null || declaration.getMrp().isBlank()) {
//             violations.add(new Violation("MISSING_MRP", "Maximum Retail Price (MRP) declaration is missing",
//                     "NON_COMPLIANT", "RULE_LM_01", null));
//         }

//         // Rule 2: Net Quantity must be present
//         if (declaration.getNetQuantity() == null || declaration.getNetQuantity().isBlank()) {
//             violations.add(new Violation("MISSING_NET_QTY", "Net Quantity declaration is missing", "NON_COMPLIANT",
//                     "RULE_LM_02", null));
//         }

//         // Rule 3: Date of Manufacture / Packing date must be present
//         if (declaration.getDateOfManufacture() == null || declaration.getDateOfManufacture().isBlank()) {
//             violations.add(new Violation("MISSING_MFG_DATE", "Date of Manufacture/Packing is missing", "NON_COMPLIANT",
//                     "RULE_LM_03", null));
//         }

//         // Rule 4: Manufacturer Name must be present
//         if (declaration.getManufacturerName() == null || declaration.getManufacturerName().isBlank()) {
//             violations.add(new Violation("MISSING_MANUFACTURER", "Manufacturer Name/Address is missing",
//                     "NON_COMPLIANT", "RULE_LM_04", null));
//         }

//         // Rule 5: Country of Origin must be present
//         if (declaration.getCountryOfOrigin() == null || declaration.getCountryOfOrigin().isBlank()) {
//             violations.add(new Violation("MISSING_ORIGIN", "Country of Origin declaration is missing", "NON_COMPLIANT",
//                     "RULE_LM_05", null));
//         }

//         // Rule 6: Consumer Care details must be present
//         if (declaration.getConsumerCareDetails() == null || declaration.getConsumerCareDetails().isBlank()) {
//             violations.add(new Violation("MISSING_CONSUMER_CARE", "Consumer Care contact details are missing",
//                     "NON_COMPLIANT", "RULE_LM_06", null));
//         }

//         return violations;
//     }
// }









package com.metrolog.backend.compliance.service;

import com.metrolog.backend.inspection.model.Violation;
import com.metrolog.backend.ocr.model.ExtractedDeclaration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ComplianceService {

    public List<Violation> evaluateDeclarations(ExtractedDeclaration declaration) {
        List<Violation> violations = new ArrayList<>();

        if (declaration == null) {
            violations.add(new Violation("MISSING_DECLARATION", "No declaration data extracted from OCR",
                    "NON_COMPLIANT", "RULE_00", null));
            return violations;
        }

        // Rule 1: MRP must be present
        if (declaration.getMrp() == null || declaration.getMrp().isBlank()) {
            violations.add(new Violation("MISSING_MRP", "Maximum Retail Price (MRP) declaration is missing",
                    "NON_COMPLIANT", "RULE_LM_01", null));
        }

        // Rule 2: Net Quantity must be present
        if (declaration.getNetQuantity() == null || declaration.getNetQuantity().isBlank()) {
            violations.add(new Violation("MISSING_NET_QTY", "Net Quantity declaration is missing", "NON_COMPLIANT",
                    "RULE_LM_02", null));
        }

        // Rule 3: Date of Manufacture / Packing date must be present
        if (declaration.getDateOfManufacture() == null || declaration.getDateOfManufacture().isBlank()) {
            violations.add(new Violation("MISSING_MFG_DATE", "Date of Manufacture/Packing is missing", "NON_COMPLIANT",
                    "RULE_LM_03", null));
        }

        // Rule 4: Manufacturer Name must be present
        if (declaration.getManufacturerName() == null || declaration.getManufacturerName().isBlank()) {
            violations.add(new Violation("MISSING_MANUFACTURER", "Manufacturer Name/Address is missing",
                    "NON_COMPLIANT", "RULE_LM_04", null));
        }

        // Rule 5: Country of Origin must be present
        if (declaration.getCountryOfOrigin() == null || declaration.getCountryOfOrigin().isBlank()) {
            violations.add(new Violation("MISSING_ORIGIN", "Country of Origin declaration is missing", "NON_COMPLIANT",
                    "RULE_LM_05", null));
        }

        // Rule 6: Consumer Care details must be present
        if (declaration.getConsumerCareDetails() == null || declaration.getConsumerCareDetails().isBlank()) {
            violations.add(new Violation("MISSING_CONSUMER_CARE", "Consumer Care contact details are missing",
                    "NON_COMPLIANT", "RULE_LM_06", null));
        }

        return violations;
    }

    /**
     * Determines overall compliance status based on the list of violations.
     * Required by Section 8 of the specification document.
     */
    public String determineComplianceStatus(List<Violation> violations) {
        if (violations == null || violations.isEmpty()) {
            return "COMPLIANT";
        }
        return "NON_COMPLIANT";
    }

    /**
     * Determines if a status should be marked as NEEDS_REVIEW when partial details
     * are captured.
     */
    public String evaluateStatusWithReviewFallback(ExtractedDeclaration declaration, List<Violation> violations) {
        if (violations.isEmpty()) {
            return "COMPLIANT";
        }

        // If more than 3 fields are present but some failed, mark for manual officer
        // review
        int capturedCount = 0;
        if (declaration.getMrp() != null && !declaration.getMrp().isBlank())
            capturedCount++;
        if (declaration.getNetQuantity() != null && !declaration.getNetQuantity().isBlank())
            capturedCount++;
        if (declaration.getDateOfManufacture() != null && !declaration.getDateOfManufacture().isBlank())
            capturedCount++;
        if (declaration.getManufacturerName() != null && !declaration.getManufacturerName().isBlank())
            capturedCount++;

        if (capturedCount >= 2 && violations.size() <= 2) {
            return "NEEDS_REVIEW";
        }

        return "NON_COMPLIANT";
    }
}