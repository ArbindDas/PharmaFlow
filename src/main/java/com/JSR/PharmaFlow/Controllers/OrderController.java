package com.JSR.PharmaFlow.Controllers;


import com.JSR.PharmaFlow.DTO.*;
import com.JSR.PharmaFlow.Entity.*;
import com.JSR.PharmaFlow.Enums.Status;
import com.JSR.PharmaFlow.Repository.MedicinesRepository;
import com.JSR.PharmaFlow.Repository.OrderItemsRepository;
import com.JSR.PharmaFlow.Repository.OrdersRepository;
import com.JSR.PharmaFlow.Repository.UsersRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping ( "/api/orders" )
@CrossOrigin ( origins = "http://localhost:5173", allowedHeaders="*" ,  allowCredentials = "true" )
public class OrderController {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrderItemsRepository orderItemsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private MedicinesRepository medicinesRepository;


    @Transactional
    @PostMapping
    public ResponseEntity < ? > createOrder(@RequestBody OrderRequest orderRequest) {
        try {
            // Get authenticated user
            Users user=usersRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Create and save order
            Orders order=new Orders();
            order.setTotalPrice(BigDecimal.valueOf(orderRequest.getTotalPrice()));
            order.setStatus(Status.PENDING);
            order.setUsers(user);
            Orders savedOrder=ordersRepository.save(order);

            // Create and save order items
            List < OrderItems > orderItems=new ArrayList <>();
            for(OrderRequest.OrderItemDto item : orderRequest.getOrderItems()){
                Medicines medicine=medicinesRepository.findById(item.getMedicineId())
                        .orElseThrow(() -> new RuntimeException("Medicine with ID "+item.getMedicineId()+" not found"));

                OrderItems orderItem=new OrderItems();
                orderItem.setQuantity(Integer.valueOf(item.getQuantity()));
                orderItem.setUnitPrice(BigDecimal.valueOf(item.getUnitPrice()));
                orderItem.setOrders(savedOrder);
                orderItem.setMedicine(medicine);

                orderItems.add(orderItem);
            }

            orderItemsRepository.saveAll(orderItems);
            return ResponseEntity.ok(savedOrder);
        } catch( Exception e ){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating order: "+e.getMessage());
        }
    }




    @GetMapping("/history")
    public ResponseEntity<?> getOrderHistory(Authentication auth) {
        try {
            String username = auth.getName();
            Users user = usersRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            List<Orders> userOrders = ordersRepository.findByUsersIdOrderByCreatedAtDesc(user.getId());

            List<OrderResponse> response = userOrders.stream()
                    .map(order -> {
                        OrderResponse orderResponse = new OrderResponse();
                        orderResponse.setOrderId(order.getId());
                        orderResponse.setTotalPrice(order.getTotalPrice());
                        orderResponse.setStatus(order.getStatus());
                        orderResponse.setOrderDate(order.getCreatedAt());

                        // Map order items
                        orderResponse.setItems(order.getOrderItemsList().stream()
                                .map(item -> OrderItemDTO.builder()
                                        .medicineId(item.getMedicine().getId())
                                        .medicineName(item.getMedicine().getName())
                                        .quantity(item.getQuantity())
                                        .unitPrice(item.getUnitPrice())
                                        .build())
                                .collect(Collectors.toList()));

                        return orderResponse;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch orders", "message", e.getMessage()));
        }
    }
}