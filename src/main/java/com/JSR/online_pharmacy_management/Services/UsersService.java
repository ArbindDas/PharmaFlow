package com.JSR.online_pharmacy_management.Services;

import com.JSR.online_pharmacy_management.Entity.Users;
import com.JSR.online_pharmacy_management.Enums.Role;
import com.JSR.online_pharmacy_management.Exception.UserAlreadyExistsException;

import com.JSR.online_pharmacy_management.Exception.UserNotFoundException;
import com.JSR.online_pharmacy_management.Repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
@Slf4j
public class UsersService {


    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsersService ( UsersRepository usersRepository , BCryptPasswordEncoder passwordEncoder ) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // create a user
    @Transactional (propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public Users saveNewUser ( Users users ) {
        try {
            log.info ( "Attempting to save or update user with username: {}" , users.getFullName () );

            // Validate user input (e.g., email check)
            if (usersRepository.existsByEmail ( users.getEmail () )) {
                throw new UserAlreadyExistsException ( "User with email " + users.getEmail () + " already exists." );
            }

            // Encode password before saving
            users.setPassword ( passwordEncoder.encode ( users.getPassword () ) );

            // Set default roles if not provided
            if (users.getRoles () == null || users.getRoles ().isEmpty ()) {
                users.setRoles ( Set.of ( Role.USER ) ); // Assuming Role is an enum
            }

            // Save the user
            Users savedUser = usersRepository.save ( users );
            log.info ( "Successfully saved or updated user with username: {}" , users.getFullName () );
            return savedUser;
        } catch (UserAlreadyExistsException e) {
            log.error ( "User already exists with email: {}" , users.getEmail () , e );
            throw e; // Rethrow the exception
        } catch (Exception e) {
            log.error ( "Unexpected error saving or updating user with username: {}" , users.getFullName () , e );
            throw new RuntimeException ( "Unexpected error occurred" , e );
        }
    }


    // get All user
    @Transactional (propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public List <?> getUsers ( ) {
        try {

            log.info ( "Retrieving the list of all users." );

            List <Users> all = usersRepository.findAll ();

            if (all.isEmpty ()) {
                log.warn ( "No users found in the database." );
            }
            log.info ( "Successfully retrieved {} users." , all.size () );

            return all;
        } catch (Exception e) {

            log.error ( "Error retrieving users list" , e );
            throw new RuntimeException ( "Failed to retrieve users list" , e );
        }
    }


    // get user by id
    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT)
    public Optional<Users>getUserById(Long id){
        try {
            log.info ( "Attempting to retrieve user with ID: {}" , id );

            Optional<Users>usersOptional = usersRepository.findById ( id );

            if (usersOptional.isPresent ()){
                log.info ( "Successfully retrieved user with ID: {}" , id );

            }else {
                log.warn ( "the user is not found by Id {}" , id );

            }
            return usersOptional;

        } catch (UserNotFoundException e) {
            throw new UserNotFoundException ("the user is not found by this id  -> {} ", e.getMessage ());
        }
    }

    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT)
        // get user by by username
    public Optional<?>getUserByFullName(String username){
        try {
            log.info ( "Attempting to retrieve user by username {}" ,username );

            Optional<Users>optionalUsers = usersRepository.findByName ( username );

            if (optionalUsers.isPresent ()){
                log.info ( "Successfully  retrieved user with username -> {} ", username );
            }else {
                log.warn ( "the user is not found with username {}" , username );
            }
            return optionalUsers;
        } catch (RuntimeException e) {
            throw new UserNotFoundException (" the user is not found by the username ->  {}" ,e.getMessage () );
        }
    }


    public boolean deleteUserById(Long id){
        try {



        } catch (Exception e) {
            throw new UserNotFoundException ( " {} -> " ,  e.getMessage () );
        }
    }

}
