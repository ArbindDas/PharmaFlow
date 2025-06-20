package com.JSR.PharmaFlow.Services;

import com.JSR.PharmaFlow.DTO.UserUpdateDTO;
import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Enums.OAuthProvider;
import com.JSR.PharmaFlow.Enums.Role;
import com.JSR.PharmaFlow.Events.EmailChangedEvent;
import com.JSR.PharmaFlow.Exception.UserAlreadyExistsException;

import com.JSR.PharmaFlow.Exception.UserNotFoundException;
import com.JSR.PharmaFlow.Repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.management.relation.RoleNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Slf4j
public class UsersService {


    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";

    private final ApplicationEventPublisher eventPublisher;

    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private final AdminService adminService;




    @Autowired
    public UsersService( ApplicationEventPublisher eventPublisher , UsersRepository usersRepository ,
                         BCryptPasswordEncoder passwordEncoder , AdminService adminService ) {
        this.eventPublisher=eventPublisher;
        this.usersRepository=usersRepository;
        this.passwordEncoder=passwordEncoder;
        this.adminService=adminService;

    }



//    @Autowired
//    public UsersService ( UsersRepository usersRepository , BCryptPasswordEncoder passwordEncoder , AdminService adminService ) {
//        this.usersRepository = usersRepository;
//        this.passwordEncoder = passwordEncoder;
//        this.adminService = adminService;
//    }



    // create a user
    @Transactional (propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public boolean saveNewUser ( Users users ) {
        try {
            log.info ("Attempting to save or update user with username: {}", users.getFullName ());
            log.info ("User roles before saving: {}" , users.getRoles ());


            Optional <Users> existingUser = usersRepository.findByEmail(users.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(users.getId())) {
                throw new UserAlreadyExistsException("User with email " + users.getEmail() + " already exists.");
            }
            users.setPassword (passwordEncoder.encode (users.getPassword ()));

            // Set default roles if not provided
            if (users.getRoles () == null || users.getRoles ().isEmpty ()) {
                users.setRoles (Set.of (Role.USER)); // Assuming Role is an enum
            }

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


    // get All user
    @Transactional (propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public List <?> getAllUsers ( ) {
        try {

            log.info ("Retrieving the list of all users.");

            List <Users> all = usersRepository.findAll ();

            if (all.isEmpty ()) {
                log.warn ("No users found in the database.");
            }
            log.info ("Successfully retrieved {} users.", all.size ());

            return all;
        }catch (Exception e) {

            log.error ("Error retrieving users list", e);
            throw new RuntimeException ("Failed to retrieve users list", e);
        }
    }


    // get user by id
    @Transactional (propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
//    @Cacheable (value = "users" , key = "#id")
    public Optional <Users> getUserById ( Long id ) {
        try {
            log.info ("Attempting to retrieve user with ID: {}", id);

            Optional <Users> usersOptional = usersRepository.findById (id);

            if (usersOptional.isPresent ()) {
                log.info ("Successfully retrieved user with ID: {}", id);

            } else {
                log.warn ("the user is not found by Id {}", id);

            }
            return usersOptional;

        }catch (UserNotFoundException e) {
            throw new UserNotFoundException ("the user is not found by this id  -> {} ", e.getMessage ());
        }
    }

    @Transactional (propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public Optional <Users> getUserByFullName ( String username ) {
        try {
            log.info ("Attempting to retrieve user by username {}", username);

            Optional <Users> optionalUsers = usersRepository.findByFullName (username);

            if (optionalUsers.isPresent ()) {
                log.info ("Successfully  retrieved user with username -> {} ", username);
            } else {
                log.warn ("the user is not found with username {}", username);
            }
            return optionalUsers;
        }catch (RuntimeException e) {
            throw new UserNotFoundException (" the user is not found by the username ->  {}", e.getMessage ());
        }
    }


    public Optional<Users> getUserByEmail ( String email ) {
        try {
            log.info ("Fetching the details by the user email: {}", email);

            return Optional.ofNullable (usersRepository.findByEmail (email)
                    .orElseThrow (( ) -> {
                        log.warn ("User not found with email: {}", email);
                        return new UserNotFoundException ("User not found with email: ", email);
                    }));

        }catch (UserNotFoundException e) {
//            throw e; // Let this bubble up to be handled by your global exception handler if you have one
            throw new UserNotFoundException ("user not found with provide email : " + e.getMessage (), email);
        }catch (Exception e) {
            log.error ("Error retrieving user details by email: {}", email, e);
            throw new RuntimeException ("Unexpected error occurred while retrieving user by email");
        }
    }

    // delete user by id
    @Transactional (propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public boolean deleteUserById ( Long id ) {
        try {
            log.info ("Attempting to retrieve user by id {}", id);

            Optional <Users> optionalUsers = usersRepository.findById (id);
            if (optionalUsers.isPresent ()) {
                usersRepository.deleteById (id);
                log.info ("Successfully deleted user with ID: {}", id);
                return true;
            } else {
                log.warn ("User with ID: {} not found, unable to delete", id);
                return false;
            }
        }catch (Exception e) {
            log.error ("Error deleting user with ID: {}", id, e);
            throw new UserNotFoundException (" the user is not found by the id  {} -> ", e.getMessage () + id);
        }
    }

    @Transactional (propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public boolean deleteUserByFullName ( String username ) {
        try {
            log.info ("Attempting to delete user bu username -> {}", username);
            Optional <Users> usersOptional = usersRepository.findByFullName (username);
            if (usersOptional.isPresent ()) {

                 usersRepository.deleteByFullName (username);

                log.info ("Successfully user delete by username {} ", username);
                return true;
            } else {
                return false;
            }

        }catch (UserNotFoundException e) {
            throw new UserNotFoundException ("user not found with username {}", e.getMessage () + username);
        }
    }



    @Transactional (propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public ResponseEntity <?> updateUsers ( Users updatedUser, String username ) {
        try {
            log.info ("updating the user : {}", username);
            Optional <Users> usersOptional = usersRepository.findByEmail (username);
            log.info("Updating user with email: {}", username);
            if (usersOptional.isPresent ()) {
                Users existingUser =usersOptional.get ();


                updatedUser.setId(existingUser.getId());

                updateFields (existingUser, updatedUser);
                adminService.saveNewUser (existingUser);
                return new ResponseEntity <> (existingUser, HttpStatus.OK);
            } else {
                return new ResponseEntity <> ("User not found", HttpStatus.NOT_FOUND);
            }
        }catch (UserNotFoundException e) {
            log.error ("Error updating user", e);
            return new ResponseEntity <> ("Failed to update user", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    public void updateFields(Users existingUser, Users updatedUser) {
        if (updatedUser.getFullName() != null && !updatedUser.getFullName().trim().isEmpty()) {
            log.info("Updating fullName to {}", updatedUser.getFullName());
            existingUser.setFullName(updatedUser.getFullName().trim());
        }

        // Update email only if it's different from current email
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty() && !updatedUser.getEmail().isBlank()) {
            if (!updatedUser.getEmail().equals(existingUser.getEmail())) {
                log.info("Updating email to {}", updatedUser.getEmail());
                existingUser.setEmail(updatedUser.getEmail());
            } else {
                log.info("Same email provided, skipping update");
            }
        }

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            existingUser.setPassword(updatedUser.getPassword());
        }

        if (updatedUser.getAuthProvider() != null) {
            log.info("Updating authProvider to {}", updatedUser.getAuthProvider());
            existingUser.setAuthProvider(updatedUser.getAuthProvider());
        }
    }

    @Transactional
    public Users updateUserByDTO(UserUpdateDTO dto, String currentUsername) {
        Users user = usersRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current user has permission to update
        if (!user.getEmail().equals(currentUsername)) {
            throw new RuntimeException("Unauthorized to update this user");
        }

        // Track email change for cache invalidation
        String oldEmail = user.getEmail();
        boolean emailChanged = !oldEmail.equals(dto.getEmail());

        // Update fields
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail()); // Always update, validation already done by DTO

        // Password update with validation
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            if (!dto.getPassword().matches(PASSWORD_PATTERN)) {
                throw new IllegalArgumentException("Password doesn't meet requirements");
            }
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // Role update
        user.setRoles(validateRoles(dto.getRoles()));

        // Auth provider
        user.setAuthProvider(dto.getAuthProvider() != null ?
                dto.getAuthProvider() :
                OAuthProvider.LOCAL);

        Users savedUser = usersRepository.save(user);

        if (emailChanged) {
            eventPublisher.publishEvent(
                    new EmailChangedEvent(oldEmail, user.getEmail())
            );
        }

        return savedUser;
    }


    private Set<Role> validateRoles(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("At least one role must be specified");
        }
        return roles; // No need for DB lookup since it's just an enum
    }




    @Transactional
    public Users adminUpdateUserByDTO(UserUpdateDTO dto, String adminUsername) {
        // 1. Verify admin exists and has permissions
        Users admin = usersRepository.findByEmail(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

//        if (!admin.getRoles().contains(Role.ADMIN)) {
//            throw new SecurityException("Only admins can perform this action");
//        }

        boolean isAdmin = admin.getRoles().stream()
                .anyMatch(role -> role == Role.ADMIN || role.name().equals("ROLE_ADMIN"));

        // 2. Validate target user exists
        Users user = usersRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Log the admin action
        log.info("Admin {} updating user {} (ID: {})",
                adminUsername,
                user.getEmail(),
                user.getId());

        // 4. Perform the update
        Users updatedUser = performUserUpdate(dto, user);

        // 5. Additional admin-specific logic if needed
        if (!user.getRoles().equals(dto.getRoles())) {
            log.info("Admin changed roles for user {} from {} to {}",
                    user.getEmail(),
                    user.getRoles(),
                    dto.getRoles());
        }

        return updatedUser;
    }

    private Users performUserUpdate(UserUpdateDTO dto, Users user) {
        // Track email change for cache invalidation
        String oldEmail = user.getEmail();
        boolean emailChanged = !oldEmail.equals(dto.getEmail());

        // Update fields
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());

        // Password update with validation
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            if (!dto.getPassword().matches(PASSWORD_PATTERN)) {
                throw new IllegalArgumentException("Password doesn't meet requirements");
            }
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // Role update
        user.setRoles(validateRoles(dto.getRoles()));

        // Auth provider
        user.setAuthProvider(dto.getAuthProvider() != null ?
                dto.getAuthProvider() :
                OAuthProvider.LOCAL);

        Users savedUser = usersRepository.save(user);

        if (emailChanged) {
            eventPublisher.publishEvent(
                    new EmailChangedEvent(oldEmail, user.getEmail())
            );
        }

        return savedUser;
    }

}
