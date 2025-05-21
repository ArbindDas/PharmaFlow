package com.JSR.online_pharmacy_management.Controllers;


import com.JSR.online_pharmacy_management.Services.AdminService;
import com.JSR.online_pharmacy_management.Services.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Slf4j
public class AdminController {

    private final AdminService adminService;
    private final UsersService usersService;

    @Autowired
    public AdminController ( AdminService adminService , UsersService usersService ) {
        this.adminService = adminService;
        this.usersService = usersService;
    }

    @GetMapping ("/get-all-users")
    public ResponseEntity <?> getAllUsers ( ) {
        try {

            Authentication authentication = SecurityContextHolder.getContext ().getAuthentication ();
            String authenticatedUser = authentication.getName ();

            log.info ("Authenticated user {} is attempting to fetch all user", authenticatedUser);

            List <?> users = usersService.getAllUsers ();
            if (!users.isEmpty ()){
                log.info ("Successfully fetched {} users.", users.size ());

                return new ResponseEntity <> (users, HttpStatus.OK);
            }else {
                return new ResponseEntity <> (HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            log.error ("Error fetching users: {}", e.getMessage (), e);
            return new ResponseEntity <> ("Failed to fetch users. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
