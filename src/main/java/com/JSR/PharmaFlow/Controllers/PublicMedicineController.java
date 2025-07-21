package com.JSR.PharmaFlow.Controllers;

import com.JSR.PharmaFlow.DTO.MedicineDto;
import com.JSR.PharmaFlow.Services.MedicineService;
import com.JSR.PharmaFlow.Services.MedicinesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/med")
@Slf4j
@CrossOrigin ("*")
public class PublicMedicineController {

    private final MedicinesService medicineService;

    @Autowired
    public PublicMedicineController(MedicinesService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping("/medicines")
    public ResponseEntity<List<MedicineDto>> getAllPublicMedicines() {
        try {
            List<MedicineDto> medicines = medicineService.getAllApprovedMedicines ();
            return ResponseEntity.ok(medicines);
        } catch (Exception e) {
            log.error("Error fetching public medicines", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}