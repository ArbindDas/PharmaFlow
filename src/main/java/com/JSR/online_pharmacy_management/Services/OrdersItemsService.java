package com.JSR.online_pharmacy_management.Services;


import com.JSR.online_pharmacy_management.Repository.OrdersItemsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrdersItemsService {

    @Autowired
    private OrdersItemsRepository ordersItemsRepository;


    private static final Logger logger = LoggerFactory.getLogger ( OrdersItemsService.class );
}
