package com.JSR.PharmaFlow.Config;

import com.JSR.PharmaFlow.Repository.MedicinesRepository;
import com.JSR.PharmaFlow.Services.MedicineService;
import com.JSR.PharmaFlow.Services.MedicineServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {
    @Bean
    public MedicineService medicineService( MedicinesRepository repo, ModelMapper mapper) {
        return new MedicineServiceImpl (repo, mapper);
    }
}