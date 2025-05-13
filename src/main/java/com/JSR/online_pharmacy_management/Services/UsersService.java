package com.JSR.online_pharmacy_management.Services;

import com.JSR.online_pharmacy_management.Repository.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UsersService {

    @Autowired
    private UsersRepository usersRepository;

    private static  final Logger logger = LoggerFactory.getLogger ( UsersService.class );




}
