package com.JSR.PharmaFlow.Controllers;

import com.JSR.PharmaFlow.DTO.MedicineDto;
import com.JSR.PharmaFlow.Entity.Medicines;
import com.JSR.PharmaFlow.Enums.Status;
import com.JSR.PharmaFlow.Services.MedicineService;
import com.JSR.PharmaFlow.Services.MedicineServiceImpl;
import com.JSR.PharmaFlow.Services.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medicines")
public class MedicineController {

    private final MedicineService medicineService; // Use interface here
    private final S3Service s3Service;

    @Autowired
    public MedicineController(MedicineService medicineService, S3Service s3Service) {
        this.medicineService = medicineService;
        this.s3Service = s3Service;
    }


    @PostMapping("/add")
    public String testAdd() {
        return "Endpoint is working";
    }

//    @PostMapping("/addMedicines")
//    @ResponseBody
//    public ResponseEntity<?> addMedicine(
//            @RequestParam("name") String name,
//            @RequestParam("description") String description,
//            @RequestParam("price") BigDecimal price,
//            @RequestParam("stock") Integer stock,
//            @RequestParam("expiryDate") LocalDate expiryDate,
//            @RequestParam(value = "image", required = false) MultipartFile imageFile,
////            @RequestParam("status") Status status,
//            @RequestParam("status") String statusStr,  // Receive as String
////            @RequestParam(value = "createdBy") Long createdBy) {
//            @RequestParam(value = "createdBy", required = false) Long createdBy) {
//
//
//
//        try {
//            // Convert string to enum
//            Status status;
//            try {
//                status = Status.valueOf(statusStr.toUpperCase());
//            } catch (IllegalArgumentException e) {
//                return ResponseEntity.badRequest()
//                        .body("Invalid status. Valid values are: " + Arrays.toString(Status.values()));
//            }
//
//
//            String imageUrl = null;
//
//            // Upload image if provided
//            if (imageFile != null && !imageFile.isEmpty()) {
//                imageUrl = s3Service.uploadFile(imageFile);
//            }
//
//            // Create medicine DTO
//            MedicineDto medicineDto = new MedicineDto();
//            medicineDto.setName(name);
//            medicineDto.setDescription(description);
//            medicineDto.setPrice(price);
//            medicineDto.setStock(stock);
//            medicineDto.setExpiryDate(expiryDate);
//            medicineDto.setImageUrl(imageUrl);
//            medicineDto.setStatus(status);
//            medicineDto.setCreatedBy(createdBy);
//            medicineDto.setCreatedAt(Instant.now());
//
//            // Save medicine
//            MedicineDto savedMedicine = medicineService.addMedicine(medicineDto);
//
//            return ResponseEntity.status(HttpStatus.CREATED).body(savedMedicine);
//
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Failed to upload image: " + e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Failed to add medicine: " + e.getMessage());
//        }
//    }


@PostMapping("/addMedicines")
public ResponseEntity<?> addMedicine(
        @RequestParam("name") String name,
        @RequestParam("description") String description,
        @RequestParam("price") BigDecimal price,
        @RequestParam("stock") Integer stock,
        @RequestParam("expiryDate") LocalDate expiryDate,
        @RequestParam(value = "image", required = false) MultipartFile imageFile,
        @RequestParam("status") String statusStr) {  // Removed createdBy parameter

    try {
        Status status = Status.valueOf(statusStr.toUpperCase());
        String imageUrl = imageFile != null ? s3Service.uploadFile(imageFile) : null;

        MedicineDto medicineDto = MedicineDto.builder()
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .expiryDate(expiryDate)
                .imageUrl(imageUrl)
                .status(status)
                .createdAt(Instant.now())
                .build();

        MedicineDto savedMedicine = medicineService.addMedicine(medicineDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMedicine);
    }
    catch (Exception e) {
        return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
    }
}



    @GetMapping("/getMedicines")
    public ResponseEntity< List <MedicineDto> > getAllMedicines() {
        try {
            List<MedicineDto> medicines = medicineService.getAllMedicines();
            return ResponseEntity.ok(medicines);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/test")
    public String test(){
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
            @RequestParam("status") Status status) {

        try {
            String imageUrl = null;
            if (imageFile != null && !imageFile.isEmpty()) {
                imageUrl = s3Service.uploadFile(imageFile);
            }

            MedicineDto medicineDto = new MedicineDto();
            medicineDto.setId(id);
            // ... set other fields ...

            MedicineDto updatedMedicine = medicineService.updateMedicine(medicineDto);
            return ResponseEntity.ok(updatedMedicine);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicine(@PathVariable Long id) {
        try {
            medicineService.deleteMedicine(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}