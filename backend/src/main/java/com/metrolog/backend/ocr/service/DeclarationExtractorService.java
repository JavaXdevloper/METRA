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

        declaration.setMrp(extractPattern(rawText,
                "(?i)(?:mrp|max(?:imum)?\\s*retail\\s*price)[:\\s]*₹?\\s*([0-9]+(?:\\.[0-9]{1,2})?)"));
        declaration.setNetQuantity(extractPattern(rawText,
                "(?i)(?:net\\s*q(?:uantit)?y|net\\s*wt|quantity)[:\\s]*([0-9]+\\s*(?:g|kg|ml|l|pcs|units?))"));
        declaration.setDateOfManufacture(extractPattern(rawText,
                "(?i)(?:mfd|mfg|manufactured|pkd|packed)[:\\s]*([0-9]{2}[/-][0-9]{2}[/-][0-9]{2,4}|[A-Za-z]{3}\\s*[0-9]{4})"));
        declaration.setManufacturerName(
                extractPattern(rawText, "(?i)(?:mfd\\s*by|manufactured\\s*by|mfg\\s*by)[:\\s]*([^\\n,]+)"));
        declaration.setCountryOfOrigin(
                extractPattern(rawText, "(?i)(?:country\\s*of\\s*origin|made\\s*in)[:\\s]*([^\\n,]+)"));
        declaration.setConsumerCareDetails(
                extractPattern(rawText, "(?i)(?:consumer\\s*care|customer\\s*care|helpline)[:\\s]*([^\\n]+)"));

        return declaration;
    }

    private String extractPattern(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}