package com.JSR.PharmaFlow.Services;

import com.JSR.PharmaFlow.DTO.MedicineDto;
import com.JSR.PharmaFlow.Entity.Medicines;
import com.JSR.PharmaFlow.Repository.MedicinesRepository;
import com.JSR.PharmaFlow.Repository.UsersRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicinesService {

    @Autowired
    private MedicinesRepository medicinesRepository;

    @Autowired
    private UsersRepository usersRepository;

    private final ModelMapper modelMapper;

    private static final Logger logger = LoggerFactory.getLogger ( MedicinesService.class );

    public MedicinesService( ModelMapper modelMapper ) {
        this.modelMapper = modelMapper;
    }


    public MedicineDto createMedicine(MedicineDto medicineDto){

        Authentication authentication =SecurityContextHolder.getContext ().getAuthentication ();
        String username = authentication.getName ();

        Medicines medicines = mapToEntity ( medicineDto );
        medicines.setCreatedAt ( Instant.now () );

        medicines.setCreatedByUser(usersRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException ("User not found")));

        Medicines savedMedicines = medicinesRepository.save ( medicines );

        return mapToDto ( savedMedicines );

    }

    public MedicineDto getMedicinesById(  Long id ){
        return MedicineDto.builder().build();
    }


    private MedicineDto mapToDto(Medicines medicines){
        return MedicineDto.builder ( )
                .id ( medicines.getId () )
                .name ( medicines.getName () )
                .description ( medicines.getDescription () )
                .price ( medicines.getPrice () )
                .stock ( medicines.getStock () )
                .expiryDate ( medicines.getExpiryDate () )
                .imageUrl ( medicines.getImageUrl () )
                .status ( medicines.getStatus () )
                .createdBy ( medicines.getCreatedBy () != null ? medicines.getCreatedByUser ().getId () : null )
                .build ( );
    }


    private Medicines mapToEntity(MedicineDto medicineDto){
        return  Medicines.builder ( )
                .id ( medicineDto.getId ( ) )
                .name ( medicineDto.getName ( ) )
                .description ( medicineDto.getDescription ( ) )
                .price ( medicineDto.getPrice () )
                .stock ( medicineDto.getStock () )
                .expiryDate ( medicineDto.getExpiryDate () )
                .imageUrl ( medicineDto.getImageUrl () )
                .status ( medicineDto.getStatus () )
                .build ( );
    }


//    public List<MedicineDto> getAllApprovedMedicines() {
//        List <Medicines> approvedMedicines = medicinesRepository.findByStatus("APPROVED");
//        return approvedMedicines.stream()
//                .map(medicine -> modelMapper.map(medicine, MedicineDto.class))
//                .collect( Collectors.toList());
//    }

public List<MedicineDto> getAllApprovedMedicines() {
    List<Medicines> approvedMedicines = medicinesRepository.findByStatusIn(List.of("APPROVED", "PLACED"));
    return approvedMedicines.stream()
            .map(medicine -> modelMapper.map(medicine, MedicineDto.class))
            .collect(Collectors.toList());
}


}
