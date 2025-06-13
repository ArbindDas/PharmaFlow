package com.JSR.PharmaFlow.Controllers;

import org.springframework.web.bind.annotation.GetMapping;

public class Root {


    @GetMapping("/")
    public String getRoot(){

        return  "root.....";
    }
}
