// package com.metrolog.backend.inspection.controller;

// import com.metrolog.backend.inspection.model.Inspection;
// import com.metrolog.backend.inspection.service.InspectionService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.multipart.MultipartFile;

// import java.security.Principal;
// import java.util.List;

// @RestController
// @RequestMapping("/api/inspections")
// public class InspectionController {

//     @Autowired
//     private InspectionService inspectionService;

//     @PostMapping
//     public ResponseEntity<Inspection> createInspection(
//             @RequestParam("productId") String productId,
//             @RequestParam("file") MultipartFile file,
//             Principal principal) throws Exception {
//         String userId = principal != null ? principal.getName() : "system";
//         Inspection inspection = inspectionService.processInspection(productId, userId, file);
//         return ResponseEntity.ok(inspection);
//     }

//     @GetMapping
//     public ResponseEntity<List<Inspection>> getAllInspections() {
//         return ResponseEntity.ok(inspectionService.getAllInspections());
//     }

//     @GetMapping("/{id}")
//     public ResponseEntity<Inspection> getInspectionById(@PathVariable String id) {
//         Inspection inspection = inspectionService.getInspectionById(id);
//         if (inspection == null) {
//             return ResponseEntity.notFound().build();
//         }
//         return ResponseEntity.ok(inspection);
//     }
// }






package com.metrolog.backend.inspection.controller;

import com.metrolog.backend.inspection.model.Inspection;
import com.metrolog.backend.inspection.service.InspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/inspections")
public class InspectionController {

    @Autowired
    private InspectionService inspectionService;

    // Handles single and multi-file package image uploads
    @PostMapping
    public ResponseEntity<Inspection> createInspection(
            @RequestParam("productId") String productId,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Principal principal) throws Exception {

        String userId = principal != null ? principal.getName() : "system";

        List<MultipartFile> fileList;
        if (files != null && files.length > 0) {
            fileList = Arrays.asList(files);
        } else if (file != null) {
            fileList = List.of(file);
        } else {
            throw new IllegalArgumentException("At least one image file must be uploaded.");
        }

        Inspection inspection = inspectionService.processMultipleInspections(productId, userId, fileList);
        return ResponseEntity.ok(inspection);
    }

    @GetMapping
    public ResponseEntity<List<Inspection>> getAllInspections() {
        return ResponseEntity.ok(inspectionService.getAllInspections());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inspection> getInspectionById(@PathVariable String id) {
        Inspection inspection = inspectionService.getInspectionById(id);
        if (inspection == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(inspection);
    }
}