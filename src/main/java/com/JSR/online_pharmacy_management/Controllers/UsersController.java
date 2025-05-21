package com.JSR.online_pharmacy_management.Controllers;


import com.JSR.online_pharmacy_management.Entity.Users;
import com.JSR.online_pharmacy_management.Exception.UserNotFoundException;
import com.JSR.online_pharmacy_management.Services.UsersService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;


@Slf4j
@RestController
@RequestMapping ( "/api/users")
public class UsersController {

    @Autowired
    private RedisTemplate < String, Users > usersRedisTemplate;

    private final UsersService usersService;

    @Autowired
    public UsersController ( UsersService usersService ) {
        this.usersService = usersService;
    }


//    @GetMapping ("/get-by-id/{id}")
//    public ResponseEntity <?> getUserById ( @PathVariable Long id ) {
//        try {
//            Authentication authentication = SecurityContextHolder.getContext ().getAuthentication ();
//            String authenticatedUser = authentication.getName ();
//            System.out.println("Authenticated user: " + authentication.getName());
//            System.out.println("Authorities: " + authentication.getAuthorities());
//            log.info ("Authenticated user: "+authentication.getName ());
//            log.info ("Authorities user: "+authentication.getAuthorities ());
//
//            // Log the attempt to retrieve the user by their ID.
//            log.info ("Authenticated user: {} is attempting to fetch user with ID: {}", authenticatedUser, id);
//
//            Optional <Users> usersOptional = usersService.getUserById (id);
//
//            if (usersOptional.isPresent ()) {
//                log.info ("Successfully retrieved user with ID: {}", id);
//                return new ResponseEntity <> (usersOptional.get (), HttpStatus.OK);
//            } else {
//                log.warn ("User with ID: {} not found", id);
//                return new ResponseEntity <> ("User with ID " + id + " not found", HttpStatus.NOT_FOUND);
//            }
//        } catch (RuntimeException e) {
//            log.error ("Error fetching user with ID: {}", id, e);
//            throw new UserNotFoundException ("User not found for ", id + e.getMessage ());
//        }
//
//    }

    @GetMapping ( "/get-by-id/{id}")
    public ResponseEntity < ? > getUserById ( @PathVariable Long id ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext ().getAuthentication ();
            String authenticatedUser = authentication.getName ();
            log.info ("Authenticated user: {}" , authenticatedUser);
            log.info ("Authorities user: {}" , authentication.getAuthorities ());

            // Redis key for the user
            String redisKey = "user:" + id;

            //  Try fetching from Redis
            Users cachedUser = usersRedisTemplate.opsForValue ().get (redisKey);

            if (cachedUser != null) {
                log.info (" User with ID {} found in Redis cache" , id);
                return new ResponseEntity <> (cachedUser , HttpStatus.OK);
            }

            //  Not found in Redis, fetch from DB
            Optional < Users > usersOptional = usersService.getUserById (id);

            if (usersOptional.isPresent ()) {
                Users user = usersOptional.get ();
                log.info (" User with ID {} fetched from DB" , id);

                //  Save to Redis for future use
                log.info("Saving user with key: {}", redisKey);
                usersRedisTemplate.opsForValue ().set (redisKey , user);

                Set <String> keys = usersRedisTemplate.keys("*");
                System.out.println("All Redis keys: " + keys);


                return new ResponseEntity <> (user , HttpStatus.OK);
            }else {
                log.warn ("❌ User with ID {} not found in DB" , id);
                return new ResponseEntity <> ("User with ID " + id + " not found" , HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) {
            log.error (" Error fetching user with ID: {}" , id , e);
            throw new UserNotFoundException ("User not found for " , id + e.getMessage ());
        }
    }


    @GetMapping ( "/get-by-fullName/{username}")
    public ResponseEntity < ? > getUserByfullName ( @PathVariable String username ) {
        try {

            Authentication authentication = SecurityContextHolder.getContext ().getAuthentication ();
            String authenticatedUser = authentication.getName ();


            log.info ("Authenticated user: {} is attempting to fetch user with username: {}" , authenticatedUser , username);

            Optional < Users > usersOptional = usersService.getUserByFullName (username);

            if (usersOptional.isPresent ()) {
                Users users = usersOptional.get ();
                log.info ("Successfully retrieved user with username: {}" , username);
                log.info ("user get  : {}" , users);
                return new ResponseEntity <> (users , HttpStatus.OK);
            }else {
                return new ResponseEntity <> ("User with username " + username + "not found " , HttpStatus.NOT_FOUND);
            }

        } catch (RuntimeException e) {
            log.error ("Error fetching user with ID: {}" , username , e);
            throw new UserNotFoundException ("User not found for " , username + e.getMessage ());

        }
    }

    @GetMapping ( "/get-by-email/{email}")
    public ResponseEntity < ? > getUserByEmail ( @PathVariable String email ) {
        try {

            Authentication authentication = SecurityContextHolder.getContext ().getAuthentication ();
            String authenticatedUser = authentication.getName ();

            Optional < Users > optionalUsers = usersService.getUserByEmail (email);

            if (optionalUsers.isPresent ()) {
                log.info ("Successfully retrieved user with email: {}" , email);
                return new ResponseEntity <> (optionalUsers.get () , HttpStatus.OK);
            }else {
                return new ResponseEntity <> ("users not found with : " + email , HttpStatus.NOT_FOUND);
            }

        } catch (RuntimeException e) {
            log.error ("Error fetching user with ID: {}" , email , e);
            throw new UserNotFoundException ("User not found for " , email + e.getMessage ());

        }
    }

    @DeleteMapping ( "/delete-by-id/{id}")
    public ResponseEntity < ? > deleteUserBYId ( @PathVariable Long id ) {
        try {

            Authentication authentication = SecurityContextHolder.getContext ().getAuthentication ();
            String authenticatedUser = authentication.getName ();

            boolean user = usersService.deleteUserById (id);
            if (user) {
                log.info ("Successfully user deleted with Id {}" , id);
            }else {
                log.error ("user not found : {}" , id);
            }
            return new ResponseEntity <> (user , HttpStatus.NO_CONTENT);

        } catch (RuntimeException e) {
            log.error ("Error deleting user with ID: {}" , id , e);
            return new ResponseEntity <> ("User not found with id : " + id , HttpStatus.NOT_FOUND);

        }
    }

    @DeleteMapping ( "/delete-by-fullName/{username}")
    public ResponseEntity < ? > deleteByFullName ( @PathVariable String username ) {
        try {

            Authentication authentication = SecurityContextHolder.getContext ().getAuthentication ();
            String authenticatedUser = authentication.getName ();

            boolean user = usersService.deleteUserByFullName (username);
            if (user) {
                log.info ("Successfully user deleted with userName {}" , username);
            }else {
                log.error ("user not found with : {}" , username);
            }
            return new ResponseEntity <> (user , HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            log.error ("Error deleting user with userName: {}" , username , e);
            return new ResponseEntity <> ("User not found with userName : " + username , HttpStatus.NOT_FOUND);
        }
    }


    @PutMapping ( "/updateUser")
    public ResponseEntity < ? > updateUser ( @RequestBody @Valid Users updatedUser ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext ().getAuthentication ();
            String currentUsername = authentication.getName (); // get logged-in user's name
            log.info ("Updating user: " + updatedUser);
            log.info ("Current logged-in username: " + currentUsername);
            return usersService.updateUsers (updatedUser , currentUsername);
        } catch (RuntimeException e) {
            return new ResponseEntity <> ("Failed to update user" , HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
