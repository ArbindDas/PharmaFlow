package com.JSR.online_pharmacy_management.Services;

import com.JSR.online_pharmacy_management.Entity.Users;
import com.JSR.online_pharmacy_management.Enums.Role;
import com.JSR.online_pharmacy_management.Exception.UserAlreadyExistsException;

import com.JSR.online_pharmacy_management.Repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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




}
