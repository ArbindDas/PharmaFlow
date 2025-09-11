

package com.JSR.PharmaFlow.Controllers;

import com.JSR.PharmaFlow.DTO.*;
import com.JSR.PharmaFlow.Entity.*;
import com.JSR.PharmaFlow.Enums.Status;
import com.JSR.PharmaFlow.Repository.MedicinesRepository;
import com.JSR.PharmaFlow.Repository.OrderItemsRepository;
import com.JSR.PharmaFlow.Repository.OrdersRepository;
import com.JSR.PharmaFlow.Repository.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping ( "/api/orders" )
@CrossOrigin ( origins = "http://localhost:5173", allowedHeaders="*" ,  allowCredentials = "true" )
@Slf4j
public class OrderController {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrderItemsRepository orderItemsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private MedicinesRepository medicinesRepository;

    // GET endpoint to fetch all orders for admin view
    @GetMapping(value = "/admin" , produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')") // Add this annotation
    public ResponseEntity<?> getAllOrders() {
        try {
            List<Orders> allOrders = ordersRepository.findAllByOrderByCreatedAtDesc();

            List<AdminOrderResponse> response = allOrders.stream()
                    .map(order -> {
                        AdminOrderResponse orderResponse = new AdminOrderResponse();
                        orderResponse.setId(order.getId());
                        orderResponse.setTotalPrice(order.getTotalPrice());
                        orderResponse.setStatus(order.getStatus());
                        orderResponse.setCreatedAt(Instant.now());
                        orderResponse.setUserName(order.getUsers().getFullName());

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

    // PUT endpoint to update order status
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, @RequestBody Map<String, String> statusUpdate) {
        try {
            String newStatus = statusUpdate.get("status");

            // Validate the status
            try {
                Status status = Status.valueOf(newStatus);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid status value"));
            }

            Orders order = ordersRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

            order.setStatus(Status.valueOf(newStatus));
            ordersRepository.save(order);

            return ResponseEntity.ok(Map.of("message", "Order status updated successfully"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Order not found", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update order status", "message", e.getMessage()));
        }
    }

//    @Transactional
//    @PostMapping
//    public ResponseEntity < ? > createOrder(@RequestBody OrderRequest orderRequest) {
//        try {
//            // Get authenticated user
//            Users user=usersRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
//                    .orElseThrow(() -> new RuntimeException("User not found"));
//
//            // Create and save order
//            Orders order=new Orders();
//            order.setTotalPrice(BigDecimal.valueOf(orderRequest.getTotalPrice()));
//            order.setStatus(Status.PENDING);
//            order.setUsers(user);
//            Orders savedOrder=ordersRepository.save(order);
//
//            // Create and save order items
//            List < OrderItems > orderItems=new ArrayList <>();
//            for(OrderRequest.OrderItemDto item : orderRequest.getOrderItems()){
//                Medicines medicine=medicinesRepository.findById(item.getMedicineId())
//                        .orElseThrow(() -> new RuntimeException("Medicine with ID "+item.getMedicineId()+" not found"));
//
//                OrderItems orderItem=new OrderItems();
//                orderItem.setQuantity(Integer.valueOf(item.getQuantity()));
//                orderItem.setUnitPrice(BigDecimal.valueOf(item.getUnitPrice()));
//                orderItem.setOrders(savedOrder);
//                orderItem.setMedicine(medicine);
//
//                orderItems.add(orderItem);
//            }
//
//            orderItemsRepository.saveAll(orderItems);
//            return ResponseEntity.ok(savedOrder);
//        } catch( Exception e ){
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error creating order: "+e.getMessage());
//        }
//    }


//    @Transactional
//    @PostMapping
//    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {
//        try {
//            log.info("Creating order with payment method: {}", orderRequest.getPaymentMethod());
//
//            // Get authenticated user
//            Users user = usersRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName())
//                    .orElseThrow(() -> new RuntimeException("User not found"));
//
//            // Create and save order
//            Orders order = new Orders();
//            order.setTotalPrice(BigDecimal.valueOf(orderRequest.getTotalPrice()));
//            order.setStatus(Status.PENDING);
//            order.setUsers(user);
//
//            // Set payment method if your Orders entity has this field
//            // order.setPaymentMethod(orderRequest.getPaymentMethod());
//
//            Orders savedOrder = ordersRepository.save(order);
//
//            // Create and save order items
//            List<OrderItems> orderItems = new ArrayList<>();
//            for (OrderRequest.OrderItemDto item : orderRequest.getOrderItems()) {
//                Medicines medicine = medicinesRepository.findById(item.getMedicineId())
//                        .orElseThrow(() -> new RuntimeException("Medicine with ID " + item.getMedicineId() + " not found"));
//
//                OrderItems orderItem = new OrderItems();
//                orderItem.setQuantity(Integer.valueOf(item.getQuantity()));
//                orderItem.setUnitPrice(BigDecimal.valueOf(item.getUnitPrice()));
//                orderItem.setOrders(savedOrder);
//                orderItem.setMedicine(medicine);
//
//                orderItems.add(orderItem);
//            }
//
//            orderItemsRepository.saveAll(orderItems);
//
//            log.info("Order created successfully with ID: {}", savedOrder.getId());
//            return ResponseEntity.ok(savedOrder);
//
//        } catch (Exception e) {
//            log.error("Error creating order: ", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error creating order: " + e.getMessage());
//        }
//    }



    @Transactional
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest,
                                         @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            log.info("=== ORDER CREATION STARTED ===");
            log.info("Received Authorization header: {}", authHeader);

            // Check authentication context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            log.info("Authentication object: {}", authentication);

            if (authentication == null) {
                log.error("Authentication is NULL");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
            }

            log.info("Authentication name: {}", authentication.getName());
            log.info("Authentication authorities: {}", authentication.getAuthorities());
            log.info("Is authenticated: {}", authentication.isAuthenticated());

            // Check if user is anonymous (not properly authenticated)
            if (authentication instanceof AnonymousAuthenticationToken) {
                log.error("User is anonymous (not properly authenticated)");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Anonymous user not allowed");
            }

            if ("anonymousUser".equals(authentication.getName())) {
                log.error("User is anonymousUser - authentication failed");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
            }

            // Get authenticated user
            Users user = usersRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> {
                        log.error("User not found in database: {}", authentication.getName());
                        return new RuntimeException("User not found");
                    });

            log.info("Creating order for user: {}", user.getEmail());

            // Create and save order
            Orders order = new Orders();
            order.setTotalPrice(BigDecimal.valueOf(orderRequest.getTotalPrice()));
            order.setStatus(Status.PENDING);
            order.setUsers(user);

            Orders savedOrder = ordersRepository.save(order);

            // Create and save order items
            List<OrderItems> orderItems = new ArrayList<>();
            for (OrderRequest.OrderItemDto item : orderRequest.getOrderItems()) {
                Medicines medicine = medicinesRepository.findById(item.getMedicineId())
                        .orElseThrow(() -> new RuntimeException("Medicine with ID " + item.getMedicineId() + " not found"));

                OrderItems orderItem = new OrderItems();
                orderItem.setQuantity(Integer.valueOf(item.getQuantity()));
                orderItem.setUnitPrice(BigDecimal.valueOf(item.getUnitPrice()));
                orderItem.setOrders(savedOrder);
                orderItem.setMedicine(medicine);

                orderItems.add(orderItem);
            }

            orderItemsRepository.saveAll(orderItems);

            log.info("Order created successfully with ID: {}", savedOrder.getId());
            return ResponseEntity.ok(savedOrder);

        } catch (Exception e) {
            log.error("Error creating order: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating order: " + e.getMessage());
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