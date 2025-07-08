package com.JSR.PharmaFlow.Controllers;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/check")
public class KeyCheckController {

    @Value("${spring.ai.openai.api-key}")
    private  String apiKey;

    @GetMapping("/show-key")
    public String keyCheck(){
        return "openAPi key "+(apiKey!=null ? "Exists" : "Missing");
    }

}



