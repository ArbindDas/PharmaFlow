package com.JSR.PharmaFlow.Services;

import com.JSR.PharmaFlow.DTO.MedicineDto;
import com.JSR.PharmaFlow.Entity.Medicines;
import com.JSR.PharmaFlow.Enums.MedicineStatus;
import com.JSR.PharmaFlow.Exception.MedicineNotFoundException;
import com.JSR.PharmaFlow.Repository.MedicinesRepository;
import com.JSR.PharmaFlow.Exception.ResourceNotFoundException;
import com.JSR.PharmaFlow.Repository.OrderItemsRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MedicineServiceImpl implements MedicineService {

    private final MedicinesRepository medicineRepository;
    private final ModelMapper modelMapper;

    private final OrderItemsRepository orderItemsRepository;

    public MedicineServiceImpl(MedicinesRepository medicineRepository,
                               ModelMapper modelMapper, OrderItemsRepository orderItemsRepository) {
        this.medicineRepository = medicineRepository;
        this.modelMapper = modelMapper;
        this.orderItemsRepository = orderItemsRepository;
    }

    @Override
    public MedicineDto addMedicine(MedicineDto medicineDto) {
        Medicines medicine = modelMapper.map(medicineDto, Medicines.class);
        Medicines savedMedicine = medicineRepository.save (medicine);
        return modelMapper.map(savedMedicine, MedicineDto.class);
    }

    @Override
    public List<MedicineDto> getAllMedicines() {
        return medicineRepository.findAll()
                .stream()
                .map(medicine -> modelMapper.map(medicine, MedicineDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public MedicineDto getMedicineById(Long id) {
        Medicines medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + id));
        return modelMapper.map(medicine, MedicineDto.class);
    }

    @Override
    public MedicineDto updateMedicine(MedicineDto medicineDto) {
        // 1. Find existing medicine or throw exception
        Medicines existingMedicine = medicineRepository.findById(medicineDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + medicineDto.getId()));

        // 2. Update only non-null fields from DTO to entity
        if (medicineDto.getName() != null) {
            existingMedicine.setName(medicineDto.getName());
        }
        if (medicineDto.getDescription() != null) {
            existingMedicine.setDescription(medicineDto.getDescription());
        }
        if (medicineDto.getPrice() != null) {
            existingMedicine.setPrice(medicineDto.getPrice());
        }
        if (medicineDto.getStock() != null) {
            existingMedicine.setStock(medicineDto.getStock());
        }
        if (medicineDto.getExpiryDate() != null) {
            existingMedicine.setExpiryDate(medicineDto.getExpiryDate());
        }
//        if (medicineDto.getStatus() != null) {
//            existingMedicine.setStatus(medicineDto.getStatus());
//        }
        if (medicineDto.getMedicineStatus()!=null){
            existingMedicine.setMedicineStatus(MedicineStatus.ADDED);
        }

        if (medicineDto.getImageUrl() != null) {
            existingMedicine.setImageUrl(medicineDto.getImageUrl());
        }

        // 3. Save the updated entity
        Medicines updatedMedicine = medicineRepository.save(existingMedicine);

        // 4. Map back to DTO and return
        return modelMapper.map(updatedMedicine, MedicineDto.class);
    }

//    @Override
//    public void deleteMedicine(Long id) {
//        Medicines medicine = medicineRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + id));
//        medicineRepository.delete(medicine);
//    }
//@Override
//public void deleteMedicine(Long id) {
//    // First, check if medicine exists
//    Medicines medicine = medicineRepository.findById(id)
//            .orElseThrow(() -> new RuntimeException("Medicine not found with id: " + id));
//
//    // Delete related order items first
//    orderItemsRepository.deleteByMedicineId(id);
//
//    // Then delete the medicine
//    medicineRepository.deleteById(id);

//}

    @Transactional
    public void deleteMedicine(Long id) {
        Medicines medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found with id: " + id));

        // Delete related order items first
        orderItemsRepository.deleteByMedicineId(id);

        // Then delete the medicine
        medicineRepository.deleteById(id);
    }
}