package com.JSR.PharmaFlow.Services;

import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Enums.Role;
import com.JSR.PharmaFlow.Exception.UserAlreadyExistsException;
import com.JSR.PharmaFlow.Repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger ( AdminService.class );

    private final UsersRepository usersRepository;


    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public AdminService ( UsersRepository usersRepository , BCryptPasswordEncoder passwordEncoder ) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // create a user
    @Transactional (propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public boolean saveNewUser ( Users users ) {
        try {
            log.info ("Attempting to save or update user with username: {}", users.getEmail ());

//            Optional <Users> existingUser = usersRepository.findByEmail(users.getEmail());
//            if (existingUser.isPresent() && !existingUser.get().getId().equals(users.getId())) {
//                throw new UserAlreadyExistsException("User with email " + users.getEmail() + " already exists.");
//            }
            // Encode password before saving
            users.setPassword (passwordEncoder.encode (users.getPassword ()));

            // Set default roles if not provided
            if (users.getRoles () == null || users.getRoles ().isEmpty ()) {
                users.setRoles (Set.of (Role.USER)); // Assuming Role is an enum
            }

            // Save the user
            usersRepository.save (users);
            log.info ("Successfully saved or updated user with username: {}", users.getFullName ());
            return true ;
        }catch (UserAlreadyExistsException e) {
            log.error ("User already exists with email: {}", users.getEmail (), e);
            throw e; // Rethrow the exception
        }catch (Exception e) {
            log.error ("Unexpected error saving or updating user with username: {}", users.getFullName (), e);
            throw new RuntimeException ("Unexpected error occurred", e);
        }
    }

//    public boolean updateTheUser( Users newUser ){
//        try {
//            log.info ("Attempting to save or update user with username: {}", newUser.getEmail ());
//
//
//            newUser.setPassword ( passwordEncoder.encode ( newUser.getPassword () ) );
//
//            if (newUser.getRoles ()==null || newUser.getRoles ().isEmpty ()) {
//
//                newUser.setRoles (Set.of ( Role.USER ) );
//
//            }
//            usersRepository.save ( newUser );
//            log.info ("Successfully saved or updated user with username: {}", newUser.getFullName ());
//            return true ;
//        } catch (RuntimeException e) {
//            // Log the error if an exception occurs
//            logger.error ( "Error saving or updating user with username: {}" , newUser.getFullName ( ) , e );
//            throw new RuntimeException ( "Failed to save or update user" , e );
//        }
//    }
}
