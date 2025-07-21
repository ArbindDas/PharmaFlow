package com.JSR.PharmaFlow.Controllers;


import com.JSR.PharmaFlow.Entity.*;
import com.JSR.PharmaFlow.Enums.Status;
import com.JSR.PharmaFlow.Repository.MedicinesRepository;
import com.JSR.PharmaFlow.Repository.OrderItemsRepository;
import com.JSR.PharmaFlow.Repository.OrdersRepository;
import com.JSR.PharmaFlow.Repository.UsersRepository;
import com.JSR.PharmaFlow.DTO.OrderRequest;
import com.JSR.PharmaFlow.DTO.OrderItemResponse;
import com.JSR.PharmaFlow.DTO.OrderDetailResponse;
import com.JSR.PharmaFlow.DTO.OrderResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class OrderController {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrderItemsRepository orderItemsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private MedicinesRepository medicinesRepository;


//    @PostMapping
//    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {
//        try {
//            // Get authenticated user
//            Users user = usersRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
//                    .orElseThrow(() -> new RuntimeException("User not found"));
//
//            // Create and save order
//            Orders order = new Orders();
//            order.setTotalPrice(BigDecimal.valueOf(orderRequest.getTotalPrice()));
//            order.setStatus(Status.PLACED);
//            order.setUsers(user);
//            Orders savedOrder = ordersRepository.save(order);
//
//            // Create and save order items
//            List<OrderItems> orderItems = new ArrayList <>();
//            for (OrderRequest.OrderItemDto item : orderRequest.getOrderItems()) {
//                Medicines medicine = medicinesRepository.findById(item.getMedicineId())
//                        .orElseThrow(() -> new RuntimeException("Medicine with ID " + item.getMedicineId() + " not found in database"));
//
//                System.out.println("Found medicine: " + medicine.getId() + " - " + medicine.getName()); // Debug log
//
//                OrderItems orderItem = new OrderItems();
//                orderItem.setQuantity(item.getQuantity());
//                orderItem.setUnitPrice(BigDecimal.valueOf(item.getUnitPrice()));
//                orderItem.setOrders(savedOrder);
//                orderItem.setMedicine(medicine);
//
//                orderItems.add(orderItem);
//            }
//
//            orderItemsRepository.saveAll(orderItems);
//            return ResponseEntity.ok(savedOrder);
//        } catch (Exception e) {
//            e.printStackTrace(); // This will show the full stack trace in logs
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error creating order: " + e.getMessage());
//        }
//    }
@Transactional
@PostMapping
public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {
    try {
        // Get authenticated user
        Users user = usersRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create and save order
        Orders order = new Orders();
        order.setTotalPrice(BigDecimal.valueOf(orderRequest.getTotalPrice()));
        order.setStatus(Status.PLACED);
        order.setUsers(user);
        Orders savedOrder = ordersRepository.save(order);

        // Create and save order items
        List<OrderItems> orderItems = new ArrayList<>();
        for (OrderRequest.OrderItemDto item : orderRequest.getOrderItems()) {
            Medicines medicine = medicinesRepository.findById(item.getMedicineId())
                    .orElseThrow(() -> new RuntimeException("Medicine with ID " + item.getMedicineId() + " not found"));

            OrderItems orderItem = new OrderItems();
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(BigDecimal.valueOf(item.getUnitPrice()));
            orderItem.setOrders(savedOrder);
            orderItem.setMedicine(medicine);

            orderItems.add(orderItem);
        }

        orderItemsRepository.saveAll(orderItems);
        return ResponseEntity.ok(savedOrder);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating order: " + e.getMessage());
    }
}




    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(Authentication auth) {
        // Get authenticated user
        String username = auth.getName();
        Users user = usersRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get all orders for the user
        List<Orders> userOrders = ordersRepository.findByUsers(user);

        // Convert to OrderResponse DTO
        List<OrderResponse> response = userOrders.stream()
                .map(order -> new OrderResponse(
                        order.getId(),
                        order.getTotalPrice(),
                        order.getStatus(),
                        order.getCreatedAt() // assuming you have this field
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrder(
            @PathVariable Long orderId,
            Authentication auth
    ) {
        // Get authenticated user
        String username = auth.getName();
        Users user = usersRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get the order and verify it belongs to the user
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUsers().equals(user)) {
            return ResponseEntity.status(403).build(); // Forbidden if order doesn't belong to user
        }

        // Convert to OrderDetailResponse DTO
        OrderDetailResponse response = new OrderDetailResponse(
                order.getId(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getCreatedAt(), // assuming you have this field
                order.getOrderItemsList().stream()
                        .map(item -> new OrderItemResponse(
                                item.getId(),
                                item.getMedicine().getName(), // assuming you have this relationship
                                item.getMedicine().getName(),       // assuming you have these fields
                                item.getQuantity(),
                                item.getUnitPrice()
                        ))
                        .collect(Collectors.toList()).reversed()
        );

        return ResponseEntity.ok(response);
    }
}