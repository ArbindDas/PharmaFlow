package com.JSR.PharmaFlow.Services;


import com.JSR.PharmaFlow.Entity.Prescription;
import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Repository.PrescriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    private static  final Logger logger = LoggerFactory.getLogger ( PrescriptionService.class );


    @Autowired
    private UsersService usersService;


    public void savePrescriptionWithUser(Prescription prescription , String username){
        try {

            Optional< Users >optionalUsers = usersService.getUserByEmail( username );
            if (optionalUsers.isPresent()){

                Users users = optionalUsers.get();

                prescription.setCreatedAt( Instant.now() );

                prescription.setUsers( users );

                Prescription savedPrescription = prescriptionRepository.save( prescription );

                users.getPrescriptionList().add( prescription );

                usersService.saveNewUser( users );

            }else {
                throw new RuntimeException("User not found");
            }

        } catch (RuntimeException e) {
            throw new RuntimeException( e );
        }
    }


    public Prescription savedPrescription(Prescription prescription) {
        logger.info("Attempting to save prescription: {}", prescription);

        try {
            Prescription saved = prescriptionRepository.save(prescription);
            logger.info("Successfully saved prescription with ID: {}", saved.getId());
            return saved;
        } catch (RuntimeException e) {
            logger.error("Failed to save prescription: {}", e.getMessage(), e);
            throw new RuntimeException("Could not save prescription", e);
        }
    }



    public Optional<?> getUserById(Long id) {
        logger.info("Attempting to retrieve user with ID: {}", id);

        try {
            Optional<Prescription> optionalPrescription = prescriptionRepository.findById(id);

            if (optionalPrescription.isPresent()) {
                logger.info("Successfully retrieved user with ID: {}", id);
            } else {
                logger.warn("User not found with ID: {}", id);
            }

            return optionalPrescription;
        } catch (RuntimeException e) {
            logger.error("Error occurred while retrieving user with ID: {}", id, e);
            throw new RuntimeException("Failed to retrieve user by ID: " + id, e);
        }
    }


    public boolean deleteById(Long id, String username) {
        logger.info("Request received to delete prescription with ID: {} for user: {}", id, username);

        boolean removed = false;

        try {
            Optional<Users> optionalUsers = usersService.getUserByEmail(username);

            if (optionalUsers.isPresent()) {
                logger.info("User found: {}", username);

                Users users = optionalUsers.get();

                removed = users.getPrescriptionList().removeIf(x -> x.getId().equals(id));
                if (removed) {
                    logger.info("Prescription with ID: {} removed from user: {}", id, username);

                    usersService.saveNewUser(users);
                    logger.info("User updated successfully after removing prescription");

                    prescriptionRepository.deleteById(id);
                    logger.info("Prescription with ID: {} deleted from repository", id);
                } else {
                    logger.warn("Prescription with ID: {} not found in user's list", id);
                }

            } else {
                logger.warn("User not found with username: {}", username);
            }

        } catch (RuntimeException e) {
            logger.error("An error occurred while deleting the prescription with ID: {} for user: {}", id, username, e);
            throw new RuntimeException("An error occurred while deleting this prescription", e);
        }

        return removed;
    }

}
