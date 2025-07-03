package com.JSR.PharmaFlow.Services;

import com.JSR.PharmaFlow.DTO.MedicineDto;
import com.JSR.PharmaFlow.Entity.Medicines;
import com.JSR.PharmaFlow.Repository.MedicinesRepository;
import com.JSR.PharmaFlow.Exception.ResourceNotFoundException;
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

    public MedicineServiceImpl(MedicinesRepository medicineRepository,
                               ModelMapper modelMapper) {
        this.medicineRepository = medicineRepository;
        this.modelMapper = modelMapper;
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
        Medicines existingMedicine = medicineRepository.findById(medicineDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + medicineDto.getId()));

        modelMapper.map(medicineDto, existingMedicine);
        Medicines updatedMedicine = medicineRepository.save(existingMedicine);
        return modelMapper.map(updatedMedicine, MedicineDto.class);
    }

    @Override
    public void deleteMedicine(Long id) {
        Medicines medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicine not found with id: " + id));
        medicineRepository.delete(medicine);
    }
}