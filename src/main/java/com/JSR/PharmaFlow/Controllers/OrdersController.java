package com.JSR.PharmaFlow.Controllers;

import com.JSR.PharmaFlow.Services.OrdersService;
import com.JSR.PharmaFlow.Services.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
@Slf4j
public class OrdersController {

    @Autowired
    private OrdersService ordersService;


    @Autowired
    private UsersService usersService;


}
