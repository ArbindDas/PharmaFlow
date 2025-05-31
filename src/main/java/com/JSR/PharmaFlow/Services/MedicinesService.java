package com.JSR.PharmaFlow.Services;

import com.JSR.PharmaFlow.Repository.MedicinesRepository;
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
