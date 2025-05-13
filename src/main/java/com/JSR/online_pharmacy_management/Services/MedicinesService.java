package com.JSR.online_pharmacy_management.Services;

import com.JSR.online_pharmacy_management.Repository.MedicinesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MedicinesService {

    @Autowired
    private MedicinesRepository medicinesRepository;

    private static final Logger logger = LoggerFactory.getLogger ( MedicinesService.class );
}
