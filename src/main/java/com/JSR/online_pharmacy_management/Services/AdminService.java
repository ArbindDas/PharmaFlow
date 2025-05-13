package com.JSR.online_pharmacy_management.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    private AdminService adminService;


    private static final Logger logger = LoggerFactory.getLogger ( AdminService.class );
}
