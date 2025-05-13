package com.JSR.online_pharmacy_management.Services;


import com.JSR.online_pharmacy_management.Repository.OrdersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrdersService {

    @Autowired
    private OrdersRepository ordersRepository;

}
