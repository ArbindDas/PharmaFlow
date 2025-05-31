package com.JSR.PharmaFlow.Controllers;


import com.JSR.PharmaFlow.Services.MedicinesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medicine")
public class MedicinesController {

    @Autowired
    private MedicinesService medicinesService;
}
