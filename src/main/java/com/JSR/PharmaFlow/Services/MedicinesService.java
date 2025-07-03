package com.JSR.PharmaFlow.Services;

import com.JSR.PharmaFlow.DTO.MedicineBasicDto;
import com.JSR.PharmaFlow.DTO.MedicineDto;
import com.JSR.PharmaFlow.Entity.Medicines;
import com.JSR.PharmaFlow.Entity.OrderItems;
import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Exception.InsufficientStockException;
import com.JSR.PharmaFlow.Repository.MedicinesRepository;
import com.JSR.PharmaFlow.Repository.OrdersItemsRepository;
import com.JSR.PharmaFlow.Repository.UsersRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class MedicinesService {

    @Autowired
    private MedicinesRepository medicinesRepository;

    @Autowired
    private UsersRepository usersRepository;

    private static final Logger logger = LoggerFactory.getLogger ( MedicinesService.class );



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



}
