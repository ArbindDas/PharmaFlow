package com.JSR.PharmaFlow.Services;

import com.JSR.PharmaFlow.DTO.MedicineDto;

import java.util.List;

public interface MedicineService {
    MedicineDto addMedicine( MedicineDto medicineDto);

    List< MedicineDto> getAllMedicines( );

    void deleteMedicine( Long id );

    MedicineDto updateMedicine( MedicineDto medicineDto );
}