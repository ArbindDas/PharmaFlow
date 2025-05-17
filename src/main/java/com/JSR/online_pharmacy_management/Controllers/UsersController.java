package com.JSR.online_pharmacy_management.Controllers;


import com.JSR.online_pharmacy_management.Entity.Users;
import com.JSR.online_pharmacy_management.Exception.UserNotFoundException;
import com.JSR.online_pharmacy_management.Services.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

import static com.mysql.cj.conf.PropertyKey.logger;

@RestController
@RequestMapping ("/api/v1/users")
@Slf4j
public class UsersController {


    private final UsersService usersService;

    @Autowired
    public UsersController ( UsersService usersService ) {
        this.usersService = usersService;
    }

    @GetMapping ("/get-all-users")
    public ResponseEntity <?> getAllUsers ( ) {
        try {

            Authentication authentication = SecurityContextHolder.getContext ().getAuthentication ();
            String authenticatedUser = authentication.getName ();

            log.info ("Authenticated user {} is attempting to fetch all user", authenticatedUser);

            List <?> users = usersService.getUsers ();

            log.info ("Successfully fetched {} users.", users.size ());

            return new ResponseEntity <> (users, HttpStatus.OK);

        } catch (Exception e) {
            log.error ("Error fetching users: {}", e.getMessage (), e);
            return new ResponseEntity <> ("Failed to fetch users. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping ("/get-by-id")
    public ResponseEntity <?> getUserById ( @PathVariable Long id ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext ().getAuthentication ();
            String authenticatedUser = authentication.getName ();

            // Log the attempt to retrieve the user by their ID.
            log.info ("Authenticated user: {} is attempting to fetch user with ID: {}", authenticatedUser, id);

            Optional <Users> usersOptional = usersService.getUserById (id);

            if (usersOptional.isPresent ()) {
                log.info ("Successfully retrieved user with ID: {}", id);
                return new ResponseEntity <> (usersOptional.get (), HttpStatus.OK);
            } else {
                log.warn ("User with ID: {} not found", id);
                return new ResponseEntity <> ("User with ID " + id + " not found", HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) {
            log.error ("Error fetching user with ID: {}", id, e);

            throw new UserNotFoundException ("User not found for ", id + e.getMessage ());
        }

    }


    @GetMapping ("/get-by-fullName")
    public ResponseEntity <?> getUserByfullName ( @PathVariable String username ) {
        try {

            Authentication authentication = SecurityContextHolder.getContext ().getAuthentication ();
            String authenticatedUser = authentication.getName ();


            log.info ("Authenticated user: {} is attempting to fetch user with username: {}", authenticatedUser, username);

            Optional <Users> usersOptional = usersService.getUserByFullName (username);

            if (usersOptional.isPresent ()) {
                Users users = usersOptional.get ();
                log.info ("Successfully retrieved user with username: {}", username);
                log.info ("user get  : {}", users);
                return new ResponseEntity <> (users, HttpStatus.OK);
            } else {
                return new ResponseEntity <> ("User with username " + username + "not found ", HttpStatus.NOT_FOUND);
            }

        } catch (RuntimeException e) {
            log.error ("Error fetching user with ID: {}", username, e);
            throw new UserNotFoundException ("User not found for ", username + e.getMessage ());

        }
    }


    @GetMapping ("/get")
    public String getUser ( ) {
        return "Abhilasha sah";
    }
}
