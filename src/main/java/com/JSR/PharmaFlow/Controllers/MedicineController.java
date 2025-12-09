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


    @GetMapping("/add")
    public ResponseEntity<Map<String, String>> testAdd() {
        log.info("Test add endpoint called");
        Map<String, String> response = Map.of(
                "Message", "Endpoint is working...."
        );
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }


    @PreAuthorize("hasRole('ADMIN')")  // Additional protection
    @PostMapping("/addMedicines")
    public ResponseEntity<?> addMedicine(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("stock") Integer stock,
            @RequestParam("expiryDate") LocalDate expiryDate,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestParam("medicineStatus") String medicineStatusStr) {

        log.info("=== START addMedicine API ===");
        log.info("Request received at: {}", Instant.now());

        try {
            // Log incoming parameters
            log.info("Incoming parameters:");
            log.info("- name: {}", name);
            log.info("- description length: {}", description != null ? description.length() : 0);
            log.info("- price: {}", price);
            log.info("- stock: {}", stock);
            log.info("- expiryDate: {}", expiryDate);
            log.info("- medicineStatus: {}", medicineStatusStr);
            log.info("- imageFile: {}", imageFile != null ?
                    String.format("%s (%d bytes, type: %s)",
                            imageFile.getOriginalFilename(),
                            imageFile.getSize(),
                            imageFile.getContentType()) :
                    "null");

            // Log authentication info
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            log.info("Authentication: {}", authentication);
            if (authentication != null) {
                log.info("- Principal: {}", authentication.getPrincipal());
                log.info("- Authorities: {}", authentication.getAuthorities());
                log.info("- Is authenticated: {}", authentication.isAuthenticated());
                log.info("- Name: {}", authentication.getName());
            }

            // Parse medicine status
            log.info("Parsing medicine status: {}", medicineStatusStr);
            MedicineStatus status = MedicineStatus.valueOf(medicineStatusStr.toUpperCase());
            log.info("Parsed status: {}", status);

            // Handle image upload
            String imageUrl = null;
            if (imageFile != null && !imageFile.isEmpty()) {
                log.info("Uploading image to S3...");
                try {
                    imageUrl = s3Service.uploadFile(imageFile);
                    log.info("Image uploaded successfully. URL: {}", imageUrl);
                } catch (Exception e) {
                    log.error("Failed to upload image to S3", e);
                    throw new RuntimeException("Failed to upload image: " + e.getMessage());
                }
            } else {
                log.info("No image file provided");
            }

            // Build medicine DTO
            log.info("Building MedicineDto...");
            MedicineDto medicineDto = MedicineDto.builder()
                    .name(name)
                    .description(description)
                    .price(price)
                    .stock(stock)
                    .expiryDate(expiryDate)
                    .imageUrl(imageUrl)
                    .medicineStatus(status)
                    .createdAt(Instant.now())
                    .build();

            log.info("MedicineDto built: {}", medicineDto);

            // Call service
            log.info("Calling medicineService.addMedicine()...");
            long startTime = System.currentTimeMillis();
            MedicineDto savedMedicine = medicineService.addMedicine(medicineDto);
            long endTime = System.currentTimeMillis();
            log.info("Service call completed in {} ms", (endTime - startTime));

            log.info("Medicine saved successfully with ID: {}", savedMedicine.getId());
            log.info("=== END addMedicine API - SUCCESS ===");

            return ResponseEntity.status(HttpStatus.CREATED).body(savedMedicine);

        } catch (IllegalArgumentException e) {
            log.error("Invalid status value: {}", medicineStatusStr, e);
            log.error("Valid status values are: {}", Arrays.toString(MedicineStatus.values()));
            return ResponseEntity.badRequest().body("Invalid status value: " + medicineStatusStr +
                    ". Valid values: " + Arrays.toString(MedicineStatus.values()));

        } catch (Exception e) {
            log.error("Error in addMedicine API", e);
            log.error("Error details - Message: {}, Cause: {}, StackTrace: {}",
                    e.getMessage(),
                    e.getCause(),
                    Arrays.toString(e.getStackTrace()));

            // Log additional context for debugging
            log.error("Failed request context:");
            log.error("- name: {}", name);
            log.error("- price: {}", price);
            log.error("- stock: {}", stock);
            log.error("- status attempted: {}", medicineStatusStr);

            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getMessage() +
                            ". Please check server logs for details.");
        } finally {
            log.info("Request processing completed at: {}", Instant.now());
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