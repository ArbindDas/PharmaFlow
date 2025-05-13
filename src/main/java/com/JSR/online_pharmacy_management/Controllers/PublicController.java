package com.JSR.online_pharmacy_management.Controllers;


import com.JSR.online_pharmacy_management.Services.PublicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @Autowired
    private PublicService publicService;

}
