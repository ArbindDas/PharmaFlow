package com.JSR.PharmaFlow.Services;


import com.JSR.PharmaFlow.Repository.OrdersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrdersService {

    @Autowired
    private OrdersRepository ordersRepository;

    private static final Logger logger = LoggerFactory.getLogger ( OrdersService.class );



}
