package com.JSR.PharmaFlow.Controllers;

import com.JSR.PharmaFlow.DTO.CustomUserDetails;
import com.JSR.PharmaFlow.DTO.MedicineDto;
import com.JSR.PharmaFlow.Entity.Medicines;
import com.JSR.PharmaFlow.Enums.MedicineStatus;
import com.JSR.PharmaFlow.Enums.Status;
import com.JSR.PharmaFlow.Exception.ResourceNotFoundException;
import com.JSR.PharmaFlow.Repository.MedicinesRepository;
import com.JSR.PharmaFlow.Services.MedicineService;
import com.JSR.PharmaFlow.Services.MedicineServiceImpl;
import com.JSR.PharmaFlow.Services.S3Service;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping ( "/api/medicines" )
@Slf4j
@PreAuthorize ("hasRole('ADMIN')") // Applies to all methods
public class MedicineController {

    private final MedicineService medicineService; // Use interface here
    private final S3Service s3Service;


    @Autowired
    private MedicinesRepository medicinesRepository;

    @Autowired
    public MedicineController( MedicineService medicineService , S3Service s3Service ) {
        this.medicineService = medicineService;
        this.s3Service = s3Service;
    }


    @PostMapping ( "/add" )
    public String testAdd( ) {
        return "Endpoint is working";
    }

    @PostMapping("/addMedicines")
    public ResponseEntity<?> addMedicine(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("stock") Integer stock,
            @RequestParam("expiryDate") LocalDate expiryDate,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestParam("medicineStatus") String medicineStatusStr) {

        try {



            MedicineStatus status = MedicineStatus.valueOf(medicineStatusStr.toUpperCase());
            String imageUrl = imageFile != null ? s3Service.uploadFile(imageFile) : null;

            MedicineDto medicineDto = MedicineDto.builder()
                    .name(name)
                    .description(description)
                    .price(price)
                    .stock(stock)
                    .expiryDate(expiryDate)
                    .imageUrl(imageUrl)
                    .medicineStatus(status)  // Using the already converted enum
                    .createdAt(Instant.now())
                    .build();

            MedicineDto savedMedicine = medicineService.addMedicine(medicineDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedMedicine);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status value: " + medicineStatusStr);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }


    @GetMapping ( "/getMedicines" )
    public ResponseEntity < List < MedicineDto > > getAllMedicines( ) {
        try {
            List < MedicineDto > medicines = medicineService.getAllMedicines ();
            return ResponseEntity.ok ( medicines );
        } catch (Exception e) {
            return ResponseEntity.status ( HttpStatus.INTERNAL_SERVER_ERROR ).build ();
        }
    }


    @GetMapping ( "/test" )
    public String test( ) {
        return "jai shrew ram";
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMedicine(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("stock") Integer stock,
            @RequestParam("expiryDate") LocalDate expiryDate,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestParam("status") String statusStr) {

        try {

            // Get authentication details
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Log authentication details
            log.info("Authentication Object: {}", authentication);
            log.info("Principal: {}", authentication.getPrincipal());
            log.info("Authorities: {}", authentication.getAuthorities());
            log.info("Is Authenticated: {}", authentication.isAuthenticated());

            // If using JWT with UserDetails
            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = ( UserDetails ) authentication.getPrincipal();
                log.info("Username: {}", userDetails.getUsername());
                log.info("Roles: {}", userDetails.getAuthorities());
            }
            // If principal is just a String (username)
            else if (authentication.getPrincipal() instanceof String) {
                log.info("Username: {}", authentication.getPrincipal());
            }

            Optional<Medicines> medicinesOptional = medicinesRepository.findById(id);
            if (medicinesOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Medicine not found with id: " + id);
            }

            // Keep existing image if no new image provided
            Medicines existing = medicinesOptional.get();
            String imageUrl = existing.getImageUrl();

            if (imageFile != null && !imageFile.isEmpty()) {
                // Delete old image if exists
                if (imageUrl != null) {
                    s3Service.deleteFile(imageUrl);
                }
                imageUrl = s3Service.uploadFile(imageFile);
            }

            MedicineStatus medicineStatus;
            try {
                medicineStatus = MedicineStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                medicineStatus = MedicineStatus.ADDED; // Default to PLACED if invalid
            }

            MedicineDto medicineDto = MedicineDto.builder()
                    .id(id)
                    .name(name)
                    .description(description)
                    .price(price)
                    .stock(stock)
                    .expiryDate(expiryDate)
                    .imageUrl(imageUrl)
                    .medicineStatus(medicineStatus)
                    .createdAt(existing.getCreatedAt()) // Keep original creation date
                    .build();

            MedicineDto updatedMedicine = medicineService.updateMedicine(medicineDto);
            return ResponseEntity.ok(updatedMedicine);
        } catch (Exception e) {
            log.error("Error updating medicine with id: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating medicine: " + e.getMessage());
        }
    }

    @DeleteMapping ( "/{id}" )
    public ResponseEntity < Void > deleteMedicine( @PathVariable Long id ) {
        try {
            medicineService.deleteMedicine ( id );
            return ResponseEntity.noContent ().build ();
        } catch (Exception e) {
            return ResponseEntity.status ( HttpStatus.INTERNAL_SERVER_ERROR ).build ();
        }
    }

}