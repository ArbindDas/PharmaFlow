package com.JSR.online_pharmacy_management.Controllers;


import com.JSR.online_pharmacy_management.Services.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UsersController {


    @Autowired
    private UsersService usersService;



    @GetMapping("/get")
    public String getUser(){
        return new String ( "Abhisek sah" );
    }
}
