// package com.metrolog.backend.ocr.service;

// import com.metrolog.backend.ocr.model.ExtractedDeclaration;
// import org.springframework.stereotype.Service;

// import java.util.regex.Matcher;
// import java.util.regex.Pattern;

// @Service
// public class DeclarationExtractorService {

//     public ExtractedDeclaration extractDeclarations(String rawText) {
//         ExtractedDeclaration declaration = new ExtractedDeclaration();

//         if (rawText == null || rawText.isEmpty()) {
//             return declaration;
//         }

//         declaration.setMrp(extractPattern(rawText,
//                 "(?i)(?:mrp|max(?:imum)?\\s*retail\\s*price)[:\\s]*₹?\\s*([0-9]+(?:\\.[0-9]{1,2})?)"));
//         declaration.setNetQuantity(extractPattern(rawText,
//                 "(?i)(?:net\\s*q(?:uantit)?y|net\\s*wt|quantity)[:\\s]*([0-9]+\\s*(?:g|kg|ml|l|pcs|units?))"));
//         declaration.setDateOfManufacture(extractPattern(rawText,
//                 "(?i)(?:mfd|mfg|manufactured|pkd|packed)[:\\s]*([0-9]{2}[/-][0-9]{2}[/-][0-9]{2,4}|[A-Za-z]{3}\\s*[0-9]{4})"));
//         declaration.setManufacturerName(
//                 extractPattern(rawText, "(?i)(?:mfd\\s*by|manufactured\\s*by|mfg\\s*by)[:\\s]*([^\\n,]+)"));
//         declaration.setCountryOfOrigin(
//                 extractPattern(rawText, "(?i)(?:country\\s*of\\s*origin|made\\s*in)[:\\s]*([^\\n,]+)"));
//         declaration.setConsumerCareDetails(
//                 extractPattern(rawText, "(?i)(?:consumer\\s*care|customer\\s*care|helpline)[:\\s]*([^\\n]+)"));

//         return declaration;
//     }

//     private String extractPattern(String text, String regex) {
//         Pattern pattern = Pattern.compile(regex);
//         Matcher matcher = pattern.matcher(text);
//         if (matcher.find()) {
//             return matcher.group(1).trim();
//         }
//         return null;
//     }
// }

package com.metrolog.backend.ocr.service;

import com.metrolog.backend.ocr.model.ExtractedDeclaration;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DeclarationExtractorService {

    public ExtractedDeclaration extractDeclarations(String rawText) {
        ExtractedDeclaration declaration = new ExtractedDeclaration();

        if (rawText == null || rawText.isEmpty()) {
            return declaration;
        }

        // 1. MRP: Matches "MRP Rs. 10.00", "MRP: ₹10", "MRP Rs.: = 20,00"
        declaration.setMrp(extractPattern(rawText,
                "(?i)(?:mrp|max(?:imum)?\\s*retail\\s*price)[:\\s*\\.=\\p{P}]*(?:rs\\.?|₹)?\\s*([0-9]+(?:[\\.,][0-9]{1,2})?)"));

        // 2. Net Quantity: Tolerates OCR typos like "Neus", "1009", "100 g", "100g"
        String netQty = extractPattern(rawText,
                "(?i)(?:net\\s*q(?:uantit)?y|net\\s*wt|quantity|neus)[:\\s]*([0-9]+[a-z0-9]*\\s*(?:g|kg|ml|l|pcs|units?|rnin)?)");
        if (netQty != null) {
            netQty = netQty.replaceAll("(?i)1009\\s*rnin", "100g");
        }
        // 2b. Fallback: scan for a bare weight token anywhere if no label is present
        if (netQty == null || netQty.isBlank()) {
            netQty = extractPattern(rawText,
                    "(?i)\\b([0-9]+(?:\\.[0-9]+)?\\s?(?:g|gm|gms|kg|ml|l|ltr)\\b)");
        }
        declaration.setNetQuantity(netQty);

        // 3. Date of Mfg: Prefers 4-digit year format ("08/2026", "8/ 2025")
        String mfgDate = extractPattern(rawText,
                "(?i)(?:mfd|mfg|manufactured|pkd|packed)(?:\\s*date)?[:\\s]*([0-9]{1,2}\\s*[/-]\\s*(?:[0-9]{4}|[0-9]{2})|[A-Za-z]{3}\\s*[0-9]{4})");

        // 3b. Fallback for "Packed on : 14-Oct-2020" or similar styles
        if (mfgDate == null || mfgDate.isBlank()) {
            mfgDate = extractPattern(rawText,
                    "(?i)(?:mfd|mfg|manufactured|pkd|packed)(?:\\s*date)?\\s*(?:on)?[:\\s]*"
                            + "([0-9]{1,2}[-/\\s][A-Za-z]{3,9}[-/\\s][0-9]{2,4})");
        }
        declaration.setDateOfManufacture(mfgDate);

        // 4. Manufacturer Name: Multi-line fallback capturing "Parle Products", "Mfg &
        // Packed by", "Birla"
        String mfgName = extractPattern(rawText,
                "(?i)(?:mfd(?:\\s*&\\s*packed)?\\s*by|manufactured\\s*by|mfg\\s*(?:&\\s*packed)?\\s*by)[:\\s*\\p{P}]*([A-Za-z0-9\\s.,-]+)");

        if (mfgName == null || mfgName.isBlank()) {
            mfgName = extractPattern(rawText, "(?i)([a-zA-Z0-9\\s]+(?:pvt\\.?|private)?\\s*ltd\\.?)");
        }

        // Secondary fallback to known brand anchors
        if (mfgName == null || mfgName.isBlank()) {
            mfgName = extractPattern(rawText, "(?i)(parle\\s*products|birla)");
        }

        // Weak branch locations fallback
        if (mfgName == null || mfgName.isBlank()) {
            String branchLocations = extractPattern(rawText,
                    "(?i)branches[:\\s]*.*?\\n\\s*([A-Za-z/\\s]+)");
            if (branchLocations != null && !branchLocations.isBlank()) {
                mfgName = "Branch location(s) only, no manufacturer name found: " + branchLocations.trim();
            }
        }

        if (mfgName != null) {
            // Truncate trailing text if OCR merges lines into consumer care or nutritional
            // data
            mfgName = mfgName.replaceAll("(?i)(?:consumer|customer)\\s*care.*", "").trim();
            mfgName = mfgName.replaceAll("(?i)carbohydrate.*", "").trim();
        }
        declaration.setManufacturerName(mfgName != null && !mfgName.isBlank() ? mfgName.trim() : null);

        // 5. Country of Origin: Handles dropped OCR characters like "an of Origin:
        // India"
        declaration.setCountryOfOrigin(extractPattern(rawText,
                "(?i)(?:(?:country|[a-z]{1,3})?\\s*of\\s*origin|origin|made\\s*in)[:\\s]*([a-zA-Z]+)"));

        // 6. Consumer Care: Tolerates missing first letter or trailing nutritional text
        // interference
        String consumerCare = extractPattern(rawText,
                "(?i)(?:c?onsumer\\s*care|customer\\s*care|helpline)[:\\s]*([^\\n]+)");

        if (consumerCare != null) {
            // Clean out accidental multiline bleeding of nutritional columns
            consumerCare = consumerCare.replaceAll("(?i)\\s+(?:carbohydrate|protein|sugar|fat|energy).*$", "").trim();
        }
        declaration.setConsumerCareDetails(consumerCare);

        return declaration;
    }

    private String extractPattern(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String result = matcher.group(matcher.groupCount() >= 2 && matcher.group(1) == null ? 2 : 1);
            return result != null ? result.trim() : null;
        }
        return null;
    }
}