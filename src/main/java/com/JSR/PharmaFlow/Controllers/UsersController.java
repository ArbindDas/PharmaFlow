package com.JSR.PharmaFlow.Controllers;


import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Exception.UserNotFoundException;
import com.JSR.PharmaFlow.Services.RedisService;
import com.JSR.PharmaFlow.Services.UsersService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.util.Optional;
import java.util.Set;


@Slf4j
@RestController
@RequestMapping ("/api/users")
public class UsersController {

    @Autowired
    private RedisTemplate<String, Users> usersRedisTemplate;

    @Autowired
    private RedisService redisService;

    private final UsersService usersService;

    @Autowired
    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @GetMapping ("/get-by-id/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedUser = authentication.getName();
            log.info("Authenticated user: {}", authenticatedUser);
            log.info("Authorities user: {}", authentication.getAuthorities());

            // Redis key for the user
            String redisKey = "user:" + id;

            //  Try fetching from Redis
            Users cachedUser = usersRedisTemplate.opsForValue().get(redisKey);

            if (cachedUser != null) {
                log.info(" User with ID {} found in Redis cache", id);
                return new ResponseEntity<>(cachedUser, HttpStatus.OK);
            }

            //  Not found in Redis, fetch from DB
            Optional<Users> usersOptional = usersService.getUserById(id);

            if (usersOptional.isPresent()) {
                Users user = usersOptional.get();
                log.info(" User with ID {} fetched from DB", id);

                //  Save to Redis for future use
                log.info("Saving user with key: {}", redisKey);
                usersRedisTemplate.opsForValue().set(redisKey, user);

                Set<String> keys = usersRedisTemplate.keys("*");
                System.out.println("All Redis keys: " + keys);


                return new ResponseEntity<>(user, HttpStatus.OK);
            } else {
                log.warn("❌ User with ID {} not found in DB", id);
                return new ResponseEntity<>("User with ID " + id + " not found", HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) {
            log.error(" Error fetching user with ID: {}", id, e);
            throw new UserNotFoundException("User not found for ", id + e.getMessage());
        }
    }


    @GetMapping ("/get-by-fullName/{username}")
    public ResponseEntity<?> getUserByfullName(@PathVariable String username) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedUser = authentication.getName();

            // Replace spaces with underscores (or any safe character)
            String safeUsername = username.replaceAll("\\s+", "_");
            String redisKey2 = "user:"+safeUsername;

            Users cachedUser = usersRedisTemplate.opsForValue().get(redisKey2);

            if (cachedUser!=null){
                log.info("User with Username{} found in redis : ", username);
                return new ResponseEntity<>(cachedUser , HttpStatus.OK);
            }

            log.info("Authenticated user: {} is attempting to fetch user with username: {}", authenticatedUser, username);

            //  Not found in Redis, fetch from DB
            Optional<Users> usersOptional = usersService.getUserByFullName(username);

            if (usersOptional.isPresent()) {
                Users users = usersOptional.get();
                log.info("Successfully retrieved user with username: {}", username);
                log.info("user get  : {}", users);

                //  Save to Redis for future use
                log.info("Saving user with key: {}", redisKey2);
                usersRedisTemplate.opsForValue().set(redisKey2 , users);

                Set<String>keys = usersRedisTemplate.keys("*");
                System.out.println("All Redis keys: " + keys);

                return new ResponseEntity<>(users, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("User with username " + username + "not found ", HttpStatus.NOT_FOUND);
            }

        } catch (RuntimeException e) {
            log.error("Error fetching user with ID: {}", username, e);
            throw new UserNotFoundException("User not found for ", username + e.getMessage());

        }
    }

    @GetMapping ("/get-by-email/{email}")
    public ResponseEntity<?> getUserByEmail(@RequestParam  String email) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedUser = authentication.getName();
            // Sanitize or encode email for Redis key

            String safeEmailKey = email.replaceAll("[^a-zA-Z0-9]", "_");
            String redisKey = "user:" + safeEmailKey;
//            String redisKey = "user:"+email;

            Users cachedUser = usersRedisTemplate.opsForValue().get(redisKey);

            if (cachedUser!=null){
                log.info("User with Username{} found in redis : ", email);
                return new ResponseEntity<>(cachedUser , HttpStatus.OK);
            }


            //  Not found in Redis, fetch from DB
            Optional<Users> optionalUsers = usersService.getUserByEmail(email);

            if (optionalUsers.isPresent()) {
                log.info("Successfully retrieved user with email: {}", email);

                Users users = optionalUsers.get();
                //  Save to Redis for future use
                log.info("Saving user with key: {}", redisKey);
                usersRedisTemplate.opsForValue().set(redisKey ,users);

                // Save to Redis for 10 minutes
//                usersRedisTemplate.opsForValue().set(redisKey, users, Duration.ofMinutes(10));

                Set<String>keys = usersRedisTemplate.keys("*");
                System.out.println("All Redis keys: " + keys);

                return new ResponseEntity<>(users, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("users not found with : " + email, HttpStatus.NOT_FOUND);
            }

        } catch (RuntimeException e) {
            log.error("Error fetching user with ID: {}", email, e);
            throw new UserNotFoundException("User not found for ", email + e.getMessage());

        }
    }

    @DeleteMapping ("/delete-by-id/{id}")
    public ResponseEntity<?> deleteUserBYId(@PathVariable Long id) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedUser = authentication.getName();

            boolean user = usersService.deleteUserById(id);
            if (user) {
                log.info("Successfully user deleted with Id {}", id);
                String redisKey = "user:" + id;
                Boolean cacheDeleted = usersRedisTemplate.delete(redisKey);
                if (cacheDeleted != null && cacheDeleted) {
                    log.info("Successfully deleted user cache with key {} from redis ", redisKey);
                } else {
                    log.warn("User cache wiht key {}  not found or could not be deleted  from redis ", redisKey);
                }

                return new ResponseEntity<>(true, HttpStatus.NO_CONTENT);
            } else {
                log.error("user not found : {}", id);
                return new ResponseEntity<>("User not found with id :" + id, HttpStatus.NOT_FOUND);
            }


        } catch (RuntimeException e) {
            log.error("Error deleting user with ID: {}", id, e);
            return new ResponseEntity<>("User not found with id : " + id, HttpStatus.NOT_FOUND);

        }
    }

    @DeleteMapping ("/delete-by-fullName/{username}")
    public ResponseEntity<?> deleteByFullName(@PathVariable String username) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedUser = authentication.getName();

            boolean user = usersService.deleteUserByFullName(username);
            if (user) {
                log.info("Successfully user deleted with userName {}", username);
                String redisKey = "user:"+username;
                Boolean cacheDeleted = usersRedisTemplate.delete(redisKey);

                if (cacheDeleted) {
                    log.info("Successfully deleted user cache with key {} from redis ", redisKey);
                } else {
                    log.warn("User cache wiht key {}  not found or could not be deleted  from redis ", redisKey);
                }
                return new ResponseEntity<>(user, HttpStatus.NO_CONTENT);
            } else {
                log.error("user not found with : {}", username);
                return new ResponseEntity<>("User not found with id :" + username, HttpStatus.NOT_FOUND);
            }

        } catch (RuntimeException e) {
            log.error("Error deleting user with userName: {}", username, e);
            return new ResponseEntity<>("User not found with userName : " + username, HttpStatus.NOT_FOUND);
        }

    }


//    @PutMapping ("/updateUser")
//    public ResponseEntity<?> updateUser(@RequestBody @Valid Users updatedUser) {
//        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            String currentUsername = authentication.getName(); // get logged-in user's name
//            log.info("Updating user: " + updatedUser);
//            log.info("Current logged-in username: " + currentUsername);
//
//            return usersService.updateUsers(updatedUser, currentUsername);
//        } catch (RuntimeException e) {
//            return new ResponseEntity<>("Failed to update user", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }


    @PutMapping("/updateUser")
    public ResponseEntity<?>updateUser(@RequestBody @Valid Users updatedUser){

        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            log.info("Updating user: " + updatedUser);
            log.info("Current logged-in username: " + currentUsername);

            ResponseEntity<?> response = usersService.updateUsers(updatedUser , currentUsername);

            if (response.getStatusCode().is2xxSuccessful()){

                // Cache update logic
                Users savedUser = (Users) response.getBody();
                assert savedUser != null;
                redisService.updateUserCache(savedUser);
            }
            return response;


        } catch (RuntimeException e) {
            return new ResponseEntity<>("Failed to update user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
