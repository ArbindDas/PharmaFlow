package com.JSR.PharmaFlow.Services;

import com.JSR.PharmaFlow.DTO.MedicineDto;
import com.JSR.PharmaFlow.Entity.Medicines;
import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Enums.MedicineStatus;
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
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
//
//@Service
//public class MedicinesService {
//
//    @Autowired
//    private MedicinesRepository medicinesRepository;
//
//    @Autowired
//    private UsersRepository usersRepository;
//
//    private final ModelMapper modelMapper;
//
//    private static final Logger logger = LoggerFactory.getLogger ( MedicinesService.class );
//
//    public MedicinesService( ModelMapper modelMapper ) {
//        this.modelMapper = modelMapper;
//    }
//
//
//    public MedicineDto createMedicine(MedicineDto medicineDto){
//
//        Authentication authentication =SecurityContextHolder.getContext ().getAuthentication ();
//        String username = authentication.getName ();
//
//
//        Users currentUser = usersRepository.findByEmail(username).orElseThrow(
//                () -> new UsernameNotFoundException("user not found")
//        );
//

/**
 * "During medicine creation, the request data is converted to an entity using mapToEntity
 * and then stored in the database."
 */
//        Medicines medicines = mapToEntity(medicineDto);
//        medicines.setCreatedAt(Instant.now());
//        medicines.setMedicineStatus(MedicineStatus.ADDED);
//        Medicines savedMedicines = medicinesRepository.save ( medicines );
//
             /*
             After creating the medicine, it sends back the data in DTO format.
              */
//        return mapToDto ( savedMedicines );
//
//    }
//
//    public MedicineDto getMedicinesById(  Long id ){
//        return MedicineDto.builder().build();
//    }
//
//
//    private MedicineDto mapToDto(Medicines medicines){
//        return MedicineDto.builder ( )
//                .id ( medicines.getId () )
//                .name ( medicines.getName () )
//                .description ( medicines.getDescription () )
//                .price ( medicines.getPrice () )
//                .stock ( medicines.getStock () )
//                .expiryDate ( medicines.getExpiryDate () )
//                .imageUrl ( medicines.getImageUrl () )
//                .medicineStatus ( medicines.getMedicineStatus () )
//                .build ( );
//    }
//
//
//    private Medicines mapToEntity(MedicineDto medicineDto){
//        return  Medicines.builder ( )
//                .id ( medicineDto.getId ( ) )
//                .name ( medicineDto.getName ( ) )
//                .description ( medicineDto.getDescription ( ) )
//                .price ( medicineDto.getPrice () )
//                .stock ( medicineDto.getStock () )
//                .expiryDate ( medicineDto.getExpiryDate () )
//                .imageUrl ( medicineDto.getImageUrl () )
//                .medicineStatus ( medicineDto.getMedicineStatus () )
//                .build ( );
//    }
//
//
//public List<MedicineDto> getAllApprovedMedicines() {
//    List<Medicines> approvedMedicines = medicinesRepository.findByMedicineStatusIn(List.of("ADDED" , "AVAILABLE"));
//    return approvedMedicines.stream()
//            .map(medicine -> modelMapper.map(medicine, MedicineDto.class))
//            .collect(Collectors.toList());
//}
//}


    /*

   todo -->  mapToEntity() → INBOUND (Client → Server → Database)
    mapToDto() → OUTBOUND (Database → Server → Client)


   todo -->  mapToEntity() (DTO → Entity)
    When: When client sends data to create/update
    Purpose: Convert API request → Database format



  todo -->  mapToDto() (Entity → DTO)
    When: When sending data back to client
    Purpose: Convert Database format → API response
     */

@Service
public class MedicinesService {

    @Autowired
    private MedicinesRepository medicinesRepository;
    @Autowired
    private UsersRepository usersRepository;

    private static final Logger looger = LoggerFactory.getLogger(MedicinesService.class);

    private final ModelMapper modelMapper;

    public MedicinesService( ModelMapper modelMapper ) {
        this.modelMapper = modelMapper;
    }

    /**
     * Creates a new medicine record in the system
     * Sets the current authenticated user, creation timestamp, and initial status
     *
     * @param medicineDto the medicine data transfer object containing medicine details
     * @return the created medicine as a DTO
     */
    public MedicineDto createMedicine( MedicineDto medicineDto ) {
        // Get the currently authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        // Retrieve user details from database
        Users currentUser = usersRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("user not found with " + username)
        );

        // Convert DTO to entity using ModelMapper

        /*
         * "During medicine creation, the request data is converted to an entity using mapToEntity or using modelMapper
         * and then stored in the database."
         */
        // So basically, the DTO is converted into an Entity
        Medicines medicines = modelMapper.map(medicineDto, Medicines.class);
        medicines.setCreatedAt(Instant.now());
        medicines.setMedicineStatus(MedicineStatus.ADDED);

        // Save to database
        Medicines saveMedicine = medicinesRepository.save(medicines);

            /*
             After creating the medicine, it sends back the data in DTO format.
              */
        // So basically, after creating a medicine, the Entity is converted back into a DTO
        return modelMapper.map(medicines, MedicineDto.class);
    }

    /**
     * Retrieves a medicine by its ID
     *
     * @param id the ID of the medicine to retrieve
     * @return a medicine DTO
     */

    public MedicineDto getMedicinesById( Long id ) {
        Medicines medicines = medicinesRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Medicine not found with id " + id)
        );

        return modelMapper.map(medicines, MedicineDto.class);
    }

    /**
     * Retrieves all medicines with approved statuses (ADDED or AVAILABLE)
     * Used to display only approved medicines to users
     *
     * @return list of approved medicine DTOs
     */

    public List<MedicineDto> getAllApprovedMedicines() {
        List<Medicines> approvedMedicines = medicinesRepository.findByMedicineStatusIn(List.of(
                "ADDED", "AVAILABLE"
        ));

        return approvedMedicines.stream()
                .map(medicines -> modelMapper.map(medicines, MedicineDto.class))
                .collect(Collectors.toList());
    }
}