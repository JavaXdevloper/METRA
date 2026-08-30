package com.metrolog.backend.ocr.service;

import com.metrolog.backend.ocr.model.ExtractedDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts mandatory declarations from raw OCR text using multi-pattern regex.
 * Covers all fields required under the Packaged Commodities Rules, 2011 (PC Rules).
 *
 * Each field has multiple patterns listed in priority order — the first match wins.
 * Patterns are designed to tolerate:
 *  - OCR artifacts (extra spaces, broken lines, substituted characters)
 *  - Varied label spellings / abbreviations found on Indian FMCG packaging
 *  - Both Rs. / INR / rupee symbol currency prefixes
 */
@Service
public class DeclarationExtractorService {

    private static final Logger log = LoggerFactory.getLogger(DeclarationExtractorService.class);

    public ExtractedDeclaration extractDeclarations(String rawText) {
        ExtractedDeclaration declaration = new ExtractedDeclaration();

        if (rawText == null || rawText.isBlank()) {
            log.warn("OCR text is empty – returning empty declaration.");
            return declaration;
        }

        // Normalise: collapse multiple spaces, unify newlines
        String text = rawText
                .replaceAll("\\r\\n|\\r", "\n")
                .replaceAll("[ \\t]{2,}", " ")
                .trim();

        log.debug("Extracting declarations from text ({} chars)", text.length());

        // ── 1. MRP ────────────────────────────────────────────────────────────────
        declaration.setMrp(extractFirst(text,
                // "MRP: Rs.45" / "MRP (Incl. of all taxes) Rs. 45" / "M.R.P.: 45.00"
                "(?i)(?:mrp|m\\.r\\.p\\.?)(?:\\s*\\(incl(?:uding|\\.)?\\s*(?:of\\s*)?all\\s*taxes?\\))?[:\\s]*(?:Rs\\.?|INR|Rs|\\u20b9)?\\s*([0-9]+(?:[.,][0-9]{1,2})?)",
                // "Maximum Retail Price: 45"
                "(?i)max(?:imum)?\\.?\\s*retail\\s*price[:\\s]*(?:Rs\\.?|INR|\\u20b9)?\\s*([0-9]+(?:[.,][0-9]{1,2})?)",
                // Fallback price mention
                "(?i)(?:price|cost|rate)[:\\s]*(?:Rs\\.?|INR|\\u20b9)?\\s*([0-9]+(?:[.,][0-9]{1,2})?)"
        ));

        // ── 2. Net Quantity ───────────────────────────────────────────────────────
        declaration.setNetQuantity(extractFirst(text,
                // "Net Qty: 500g" / "Net Weight: 1 kg" / "Net Content: 750 ml"
                "(?i)net\\s*(?:qty|quantity|wt\\.?|weight|vol\\.?|volume|content|contents?)[:\\s]*([0-9]+(?:[.,][0-9]+)?\\s*(?:g|gm|gms|gram|grams|kg|kgs|kilogram|ml|mL|millilitre|l|L|litre|ltr|oz|lb|pcs|pieces?|units?|tabs?|tablets?|capsules?|sachets?|pouches?|nos?|bags?))",
                // "Quantity: 200 g"
                "(?i)(?:quantity|contents?)[:\\s]*([0-9]+(?:[.,][0-9]+)?\\s*(?:g|gm|gms|kg|ml|l|pcs|units?))",
                // Standalone numeric quantity line: "500 g" or "1 kg"
                "(?<![0-9])([0-9]+(?:[.,][0-9]+)?\\s*(?:g|gm|gms|gram|kg|ml|mL|litre|ltr|pcs|units?|nos?))(?![0-9])"
        ));

        // ── 3. Date of Manufacture / Packing ─────────────────────────────────────
        declaration.setDateOfManufacture(extractFirst(text,
                // "Mfd: 03/2024" / "Mfg: 15-06-2024"
                "(?i)(?:mfd|mfg|mfd\\.?|mfg\\.?|manufactured|manufacturing|pkd|packing|packed|production|produced)[:\\s.]*([0-9]{1,2}[/\\-][0-9]{1,2}[/\\-][0-9]{2,4})",
                // "Mfd: Jan 2024"
                "(?i)(?:mfd|mfg|manufactured|pkd|packed|production)[:\\s.]*([A-Za-z]{3,9}[.\\s]*[0-9]{4})",
                // "DOM: 2024-03"
                "(?i)(?:dom|date\\s*of\\s*m(?:anufacture|fg|fd))[:\\s]*([0-9]{4}[/\\-][0-9]{1,2}(?:[/\\-][0-9]{1,2})?)",
                // "Mfg Date: March 2024"
                "(?i)mf(?:g|d)\\.?\\s*date[:\\s]*([A-Za-z]+\\s+[0-9]{4})"
        ));

        // ── 4. Expiry Date ────────────────────────────────────────────────────────
        declaration.setExpiryDate(extractFirst(text,
                "(?i)(?:exp(?:iry|\\.)?|expiration|use\\s*by|use\\s*before|exp\\.?\\s*date)[:\\s.]*([0-9]{1,2}[/\\-][0-9]{1,2}[/\\-][0-9]{2,4})",
                "(?i)(?:exp(?:iry|\\.)?|expiration|use\\s*by|use\\s*before)[:\\s.]*([A-Za-z]{3,9}[.\\s]*[0-9]{4})",
                "(?i)(?:best\\s*before|bb)[:\\s.]*([0-9]{1,2}[/\\-][0-9]{1,2}[/\\-][0-9]{2,4})",
                "(?i)(?:best\\s*before|bb)[:\\s.]*([A-Za-z]{3,9}[.\\s]*[0-9]{4})"
        ));

        // ── 5. Best Before (shelf life) ───────────────────────────────────────────
        declaration.setBestBefore(extractFirst(text,
                "(?i)best\\s*before[:\\s]*([0-9]+\\s*(?:months?|years?|days?|weeks?)(?:\\s*from\\s*(?:date\\s*of\\s*)?(?:manufacture|mfg|mfd|packing|pkd))?)",
                "(?i)consume\\s*within[:\\s]*([0-9]+\\s*(?:months?|years?|days?))",
                "(?i)shelf\\s*life[:\\s]*([0-9]+\\s*(?:months?|years?|days?))"
        ));

        // ── 6. Manufacturer Name ──────────────────────────────────────────────────
        declaration.setManufacturerName(extractFirst(text,
                "(?i)mf(?:g|d)\\.?\\s*by[:\\s]*([^\\n,;]{5,80})",
                "(?i)manufactured\\s*by[:\\s]*([^\\n,;]{5,80})",
                "(?i)manufacturer[:\\s]*([^\\n,;]{5,80})",
                "(?i)mfr[:\\s]*([^\\n,;]{5,80})"
        ));

        // ── 7. Manufacturer Address ───────────────────────────────────────────────
        declaration.setManufacturerAddress(extractFirst(text,
                // Address line(s) following manufacturer name
                "(?i)(?:mf(?:g|d)\\.?\\s*by|manufactured\\s*by|manufacturer)[^\\n]*\\n([^\\n]{10,120}(?:\\n[^\\n]{5,120})?)",
                "(?i)(?:factory|plant|unit\\s*(?:no\\.?|address)?)[:\\s]*([^\\n]{10,120})",
                "(?i)(?:registered\\s*)?address[:\\s]*([^\\n]{10,120})"
        ));

        // ── 8. Packer Name ────────────────────────────────────────────────────────
        declaration.setPackerName(extractFirst(text,
                "(?i)packed\\s*by[:\\s]*([^\\n,;]{5,80})",
                "(?i)packer[:\\s]*([^\\n,;]{5,80})",
                "(?i)pkd\\.?\\s*by[:\\s]*([^\\n,;]{5,80})"
        ));

        // ── 9. Importer Name ──────────────────────────────────────────────────────
        declaration.setImporterName(extractFirst(text,
                "(?i)imported\\s*by[:\\s]*([^\\n,;]{5,80})",
                "(?i)importer[:\\s]*([^\\n,;]{5,80})",
                "(?i)sole\\s*importer[:\\s]*([^\\n,;]{5,80})",
                "(?i)marketing\\s*(?:company|office)[:\\s]*([^\\n,;]{5,80})"
        ));

        // ── 10. Country of Origin ─────────────────────────────────────────────────
        declaration.setCountryOfOrigin(extractFirst(text,
                "(?i)country\\s*of\\s*origin[:\\s]*([A-Za-z][A-Za-z ]{2,38})",
                "(?i)made\\s*in[:\\s]*([A-Za-z][A-Za-z ]{2,28})",
                "(?i)origin[:\\s]*([A-Za-z][A-Za-z ]{2,28})",
                "(?i)product\\s*of[:\\s]*([A-Za-z][A-Za-z ]{2,28})"
        ));

        // ── 11. Consumer Care Details ─────────────────────────────────────────────
        declaration.setConsumerCareDetails(extractFirst(text,
                "(?i)(?:consumer|customer)\\s*care[:\\s]*([^\\n]{5,120})",
                "(?i)helpline[:\\s]*([^\\n]{5,80})",
                "(?i)toll[\\s\\-]*free[:\\s]*([0-9][^\\n]{4,20})",
                "(?i)contact\\s*us[:\\s]*([^\\n]{5,80})",
                "(?i)(?:tel|ph|phone|mob(?:ile)?)[.:\\s]*([+]?[0-9][0-9\\s\\-]{8,18})"
        ));

        // ── 12. Product Name ──────────────────────────────────────────────────────
        declaration.setProductName(extractFirst(text,
                "(?i)product\\s*name[:\\s]*([^\\n]{3,80})",
                "(?i)item\\s*name[:\\s]*([^\\n]{3,80})",
                "(?i)\\bname[:\\s]*([^\\n]{3,60})"
        ));

        // ── 13. Batch / Lot Number ────────────────────────────────────────────────
        declaration.setBatchNumber(extractFirst(text,
                "(?i)(?:batch|lot)\\s*(?:no\\.?|number|#)[:\\s.]*([A-Z0-9/\\-]{2,25})",
                "(?i)(?:b\\.?no\\.?|lot\\.?no\\.?)[:\\s.]*([A-Z0-9/\\-]{2,25})",
                "(?i)(?:batch|lot)[\\s#:]*([A-Z0-9\\-]{3,20})"
        ));

        // ── 14. FSSAI License Number ──────────────────────────────────────────────
        declaration.setFssaiLicense(extractFirst(text,
                // FSSAI numbers are 14 digits
                "(?i)(?:fssai|fssai\\s*lic(?:ense)?)[:\\s.#no]*([0-9]{14})",
                // OCR may add spaces inside the number
                "(?i)fssai[^0-9]*([0-9][0-9 ]{11,17}[0-9])",
                "(?i)food\\s*safety[^0-9]*([0-9]{10,14})"
        ));

        // ── 15. Barcode / EAN ─────────────────────────────────────────────────────
        declaration.setBarcode(extractFirst(text,
                "(?i)(?:ean|upc|gtin|barcode|bar\\s*code)[:\\s]*([0-9]{8,14})",
                "(?i)\\b([0-9]{12,13})\\b"
        ));

        // ── 16. Ingredients / Composition ─────────────────────────────────────────
        declaration.setIngredients(extractFirst(text,
                "(?i)ingredients?[:\\s]*([^\\n]{10,300})",
                "(?i)contains?[:\\s]*([^\\n]{10,200})",
                "(?i)composition[:\\s]*([^\\n]{10,200})"
        ));

        // ── 17. Unit Sale Price ───────────────────────────────────────────────────
        declaration.setUnitSalePrice(extractFirst(text,
                "(?i)(?:unit\\s*(?:sale\\s*)?price|usp)[:\\s]*(?:Rs\\.?|INR|\\u20b9)?\\s*([0-9]+(?:[.,][0-9]{1,2})?)",
                "(?i)price\\s*per\\s*unit[:\\s]*(?:Rs\\.?|INR|\\u20b9)?\\s*([0-9]+(?:[.,][0-9]{1,2})?)"
        ));

        // ── 18. Raw Material Origin ───────────────────────────────────────────────
        declaration.setRawMaterialOrigin(extractFirst(text,
                "(?i)raw\\s*materials?\\s*origin[:\\s]*([^\\n]{3,60})",
                "(?i)sourced\\s*from[:\\s]*([^\\n]{3,60})"
        ));

        log.debug("Extraction complete: mrp={}, netQty={}, mfgDate={}, exp={}, mfr={}, origin={}",
                declaration.getMrp(), declaration.getNetQuantity(),
                declaration.getDateOfManufacture(), declaration.getExpiryDate(),
                declaration.getManufacturerName(), declaration.getCountryOfOrigin());

        return declaration;
    }

    /**
     * Tries each regex pattern in order and returns the first capturing group of the
     * first successful match. Returns {@code null} if no pattern matches.
     */
    private String extractFirst(String text, String... patterns) {
        for (String regex : patterns) {
            try {
                Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    String value = matcher.group(1).trim();
                    // Discard noise-only matches (too short or pure punctuation)
                    if (!value.isEmpty() && !value.matches("[^a-zA-Z0-9]+")) {
                        return value;
                    }
                }
            } catch (Exception e) {
                log.warn("Regex error for pattern [{}]: {}", regex, e.getMessage());
            }
        }
        return null;
    }
}