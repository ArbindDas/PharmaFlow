package com.JSR.online_pharmacy_management.Services;


import com.JSR.online_pharmacy_management.Repository.PrescriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    private static  final Logger logger = LoggerFactory.getLogger ( PrescriptionService.class );
}
