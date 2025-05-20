package com.JSR.online_pharmacy_management.Controllers;


import com.JSR.online_pharmacy_management.Entity.Users;
import com.JSR.online_pharmacy_management.Services.PublicService;
import com.JSR.online_pharmacy_management.Services.UsersService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping ("/api/public")
public class PublicController {

    public final UsersService usersService;

    @Autowired
    public PublicController ( UsersService usersService ) {
        this.usersService = usersService;
    }

    @PostMapping ("/create-user")
    public ResponseEntity <?> createNewUsers ( @RequestBody @Valid Users users ) {
        try {
            log.info ("Creating new user: {}", users);
            boolean isUserCreated = usersService.saveNewUser (users);

            if (isUserCreated) {
                log.info ("User created successfully: {}", users);
                return new ResponseEntity <> (true , HttpStatus.CREATED);
            }else {
                log.warn ("User creation failed for: {}", users);
                return new ResponseEntity <> (HttpStatus.BAD_REQUEST);
            }

        } catch (RuntimeException e) {
            log.error ("Error occurred while creating user: {}", e.getMessage (), e);
            return new ResponseEntity <> ("An error occurred while creating the user.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping ("/test")
    public ResponseEntity<?> testPublic() {
        return ResponseEntity.ok("Hello from public");
    }



    @PostMapping ("/signup")
    public ResponseEntity <?> Signup ( @RequestBody @Valid Users users ) {
        try {

            boolean saveduser = usersService.saveNewUser (users);
            if (saveduser) {
                return new ResponseEntity <> (saveduser, HttpStatus.CREATED);

            }else {
                return new ResponseEntity <> (HttpStatus.BAD_REQUEST);
            }

        } catch (RuntimeException e) {
            throw new RuntimeException (e);
        }
    }


//    @PostMapping("/login")
//    public ResponseEntity<?>loginUser(@RequestBody @Valid Users users){
//        try {
//
//        } catch (RuntimeException e) {
//            throw new RuntimeException (e);
//
//        }
//    }

}
