package com.JSR.PharmaFlow.Config;

import com.JSR.PharmaFlow.Repository.MedicinesRepository;
import com.JSR.PharmaFlow.Repository.OrderItemsRepository;
import com.JSR.PharmaFlow.Services.MedicineService;
import com.JSR.PharmaFlow.Services.MedicineServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public MedicineService medicineService(MedicinesRepository medicinesRepository,
                                           ModelMapper modelMapper,
                                           OrderItemsRepository orderItemsRepository) {
        return new MedicineServiceImpl(medicinesRepository, modelMapper, orderItemsRepository);
    }

}