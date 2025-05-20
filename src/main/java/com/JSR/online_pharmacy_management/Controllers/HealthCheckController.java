package com.JSR.online_pharmacy_management.Controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/health")
@Slf4j
public class HealthCheckController {


    @GetMapping("/print")
    public  String showHealth(){
        return  new String ( "jai shree ram " );
    }


    @GetMapping ("/testHealth")
    public String   testPublic() {
        return "jai";
    }


    public  record  Student(String name){

    }

    @GetMapping("/printStudent")
    public  Student showStudent(){
        return new Student ( "Arbind Das" );
    }


    public  record AddTwoNumbers(int ans){

    }



    @GetMapping("/add")
    public AddTwoNumbers addTwoNumbers(
            @RequestParam (defaultValue = "0") int num1,
            @RequestParam (defaultValue = "0") int num2
    ) {
        int ans = num1 + num2;
        return new AddTwoNumbers(ans);
    }


    @GetMapping("/addTwo")
    public  AddTwoNumbers addTwoNumbersTwo(
            @RequestParam (defaultValue = "0") int number1,
            @RequestParam(defaultValue = "0") int number2
    ){
        int ans = number1 + number2;
        return new AddTwoNumbers ( ans );
    }


}
