package com.JSR.online_pharmacy_management.Controllers;


import com.JSR.online_pharmacy_management.Services.OrdersItemsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders-item")
public class OrdersItemsController {


    @Autowired
    private OrdersItemsService ordersItemsService;
}
