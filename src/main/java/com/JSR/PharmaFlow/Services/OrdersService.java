package com.JSR.PharmaFlow.Services;

import com.JSR.PharmaFlow.DTO.CreateOrderDTO;
import com.JSR.PharmaFlow.DTO.OrderItemDTO;
import com.JSR.PharmaFlow.DTO.OrderItemRequest;
import com.JSR.PharmaFlow.DTO.OrderRequest;
import com.JSR.PharmaFlow.Entity.*;
import com.JSR.PharmaFlow.Enums.Status;
import com.JSR.PharmaFlow.Repository.MedicinesRepository;
import com.JSR.PharmaFlow.Repository.OrderItemsRepository;
import com.JSR.PharmaFlow.Repository.OrdersRepository;
import com.JSR.PharmaFlow.Repository.UsersRepository;
import com.JSR.PharmaFlow.Services.kafka.OrderNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class OrdersService {




    private static final Logger logger = LoggerFactory.getLogger(OrdersService.class);

    @Autowired
    private OrdersRepository ordersRepository;



    @Autowired
    private OrderNotificationService orderNotificationService;



    @Autowired
    private OrderItemsRepository orderItemsRepository;

    @Autowired
    private MedicinesRepository medicinesRepository;

    @Autowired
    private UsersRepository usersRepository;


    @Autowired
    private UsersService usersService;

    @Transactional(propagation = Propagation.REQUIRED)
    public void saveOrderWithUser(Orders orders, String username) {
        try {
            Optional<Users> optionalUsers = usersService.getUserByEmail(username);

            if (optionalUsers.isPresent()) {
                Users users = optionalUsers.get();
                orders.setCreatedAt(Instant.now());
                orders.setUsers(users);

                Orders savedOrders = ordersRepository.save(orders);
                users.getOrdersList().add(savedOrders);

                usersService.saveNewUser(users);
            } else {
                throw new RuntimeException("User not found");
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public Orders savedOrder(Orders orders) {
        try {
            logger.info("Attempting to save journal entry without user association...");
            Orders savedOrder = ordersRepository.save(orders);
            logger.info("Journal entry saved successfully with ID: {}", savedOrder.getId());
            return savedOrder;
        } catch (RuntimeException e) {
            logger.error("Error occurred while saving journal entry: ", e);
            throw new RuntimeException("Failed to save journal entry", e);
        }
    }

    public List<?> ordersList() {
        try {
            logger.info("Fetching all order list...");
            List<Orders> orders = ordersRepository.findAll();
            logger.info("Successfully retrieved {} journal entries", orders.size());
            return orders;
        } catch (RuntimeException e) {
            logger.error("Error occurred while fetching journal entries: ", e);
            throw new RuntimeException("Failed to fetch journal entries", e);
        }
    }

    public Optional<?> findById(@PathVariable Long id) {
        try {
            Optional<Orders> optionalOrders = ordersRepository.findById(id);
            if (optionalOrders.isPresent()) {
                logger.info("Orders found with ID: {}", id);
            } else {
                logger.warn("Orders not found with ID: {}", id);
            }
            return optionalOrders;
        } catch (RuntimeException e) {
            logger.error("Error occurred while finding journal entry with ID: " + id, e);
            throw new RuntimeException("Failed to find journal entry", e);
        }
    }

    public boolean deleteById(Long id, String username) {
        boolean removed = false;
        try {
            Optional<Users> optionalUsers = usersService.getUserByEmail(username);

            if (optionalUsers.isPresent()) {
                Users users = optionalUsers.get();
                removed = users.getOrdersList().removeIf(order -> order.getId().equals(id));

                if (removed) {
                    usersService.saveNewUser(users);
                    ordersRepository.deleteById(id);
                }
            } else {
                throw new RuntimeException("User not found with username: " + username);
            }
        } catch (RuntimeException e) {
            logger.error("An error occurred while deleting the order with ID: " + id, e);
            throw new RuntimeException("An error occurred while deleting this id", e);
        }
        return removed;
    }




    public Orders createOrder(List< OrderItemDTO > itemDtos) {
        Orders order = new Orders();
        order.setStatus(Status.PLACED);
        order.setTotalPrice(calculateTotal(itemDtos));

        // Add order items
        for ( OrderItemDTO itemDto : itemDtos) {
            OrderItems item = new OrderItems(
                    itemDto.getQuantity(),
                    itemDto.getUnitPrice(),
                    order
            );
            order.setOrderItemsList(List.of(item));
        }

        return ordersRepository.save(order);
    }

//    @Transactional
//    public Orders createOrder(OrderRequest orderRequest, String username) {
//        log.info("Creating order for user: {}", username);
//
//        // Get authenticated user
//        Users user = usersRepository.findByEmail(username)
//                .orElseThrow(() -> {
//                    log.error("User not found in database: {}", username);
//                    return new RuntimeException("User not found");
//                });
//
//        // Create and save order
//        Orders order = new Orders();
//        order.setTotalPrice(BigDecimal.valueOf(orderRequest.getTotalPrice()));
//        order.setStatus(Status.PENDING);
//        order.setUsers(user);
//
//        // Set payment information
//        order.setPaymentMethod(orderRequest.getPaymentMethod());
//
//        // Create payment details
//        PaymentDetails paymentDetails = new PaymentDetails();
//        paymentDetails.setAmount(BigDecimal.valueOf(orderRequest.getTotalPrice()));
//        paymentDetails.setPaymentDate(Instant.now());
//        paymentDetails.setPaymentMethod(orderRequest.getPaymentMethod());
//
//        // Set payment status based on method
//        if ("cod".equalsIgnoreCase(orderRequest.getPaymentMethod())) {
//            paymentDetails.setPaymentStatus("pending");
//        } else {
//            paymentDetails.setPaymentStatus("completed");
//        }
//
//        // Set Stripe payment intent ID if provided
//        if (orderRequest.getPaymentIntentId() != null && !orderRequest.getPaymentIntentId().isEmpty()) {
//            paymentDetails.setPaymentIntentId(orderRequest.getPaymentIntentId());
//            paymentDetails.setTransactionId(orderRequest.getPaymentIntentId());
//        }
//
//        order.setPaymentDetails(paymentDetails);
//
//        // Store username for admin panel
//        order.setUserName(user.getFullName());
//
//        Orders savedOrder = ordersRepository.save(order);
//
//        // Create and save order items
//        List<OrderItems> orderItems = new ArrayList<>();
//        for (OrderRequest.OrderItemDto item : orderRequest.getOrderItems()) {
//            Medicines medicine = medicinesRepository.findById(item.getMedicineId())
//                    .orElseThrow(() -> new RuntimeException("Medicine with ID " + item.getMedicineId() + " not found"));
//
//            OrderItems orderItem = new OrderItems();
//            orderItem.setQuantity(Integer.valueOf(item.getQuantity()));
//            orderItem.setUnitPrice(BigDecimal.valueOf(item.getUnitPrice()));
//            orderItem.setOrders(savedOrder);
//            orderItem.setMedicine(medicine);
//
//            orderItems.add(orderItem);
//
//            // Update medicine stock
//            medicine.setStock(medicine.getStock() - Integer.parseInt(item.getQuantity()));
//            medicinesRepository.save(medicine);
//        }
//
//        orderItemsRepository.saveAll(orderItems);
//
//        log.info("Order created successfully with ID: {}", savedOrder.getId());
//
//        // Send Kafka notification
//        try {
//            orderNotificationService.sendOrderConfirmation(savedOrder.getId(), user.getEmail());
//            log.info("Order confirmation notification sent via Kafka for order ID: {}", savedOrder.getId());
//        } catch (Exception e) {
//            log.error("Failed to send Kafka notification for order {}: {}", savedOrder.getId(), e.getMessage());
//            // Don't fail the order creation if Kafka fails
//        }
//
//        return savedOrder;
//    }
//    public List<Orders> getOrdersForAdmin() {
//        return ordersRepository.findAllByOrderByCreatedAtDesc();
//    }


    @Transactional
    public Orders createOrder(OrderRequest orderRequest, String username) {
        log.info("Creating order for user: {}", username);

        // Get authenticated user
        Users user = usersRepository.findByEmail(username)
                .orElseThrow(() -> {
                    log.error("User not found in database: {}", username);
                    return new RuntimeException("User not found");
                });

        // Create and save order
        Orders order = new Orders();
        order.setTotalPrice(BigDecimal.valueOf(orderRequest.getTotalPrice()));
        order.setStatus(Status.PENDING);
        order.setUsers(user);

        // ⭐ Set payment method on the Order entity (NOT in PaymentDetails)
        order.setPaymentMethod(orderRequest.getPaymentMethod());

        // Create payment details WITHOUT paymentMethod
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setAmount(BigDecimal.valueOf(orderRequest.getTotalPrice()));
        paymentDetails.setPaymentDate(Instant.now());
        // ❌ DON'T set payment method here - paymentDetails.setPaymentMethod(orderRequest.getPaymentMethod());

        // Set payment status based on method
        if ("cod".equalsIgnoreCase(orderRequest.getPaymentMethod())) {
            paymentDetails.setPaymentStatus("pending");
        } else {
            paymentDetails.setPaymentStatus("completed");
        }

        // Set Stripe payment intent ID if provided
        if (orderRequest.getPaymentIntentId() != null && !orderRequest.getPaymentIntentId().isEmpty()) {
            paymentDetails.setPaymentIntentId(orderRequest.getPaymentIntentId());
            paymentDetails.setTransactionId(orderRequest.getPaymentIntentId());
        }

        order.setPaymentDetails(paymentDetails);

        // Store username for admin panel
        order.setUserName(user.getFullName());

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

            // Update medicine stock
            medicine.setStock(medicine.getStock() - Integer.parseInt(item.getQuantity()));
            medicinesRepository.save(medicine);
        }

        orderItemsRepository.saveAll(orderItems);

        log.info("Order created successfully with ID: {}", savedOrder.getId());

        // Send Kafka notification
        try {
            orderNotificationService.sendOrderConfirmation(savedOrder.getId(), user.getEmail());
            log.info("Order confirmation notification sent via Kafka for order ID: {}", savedOrder.getId());
        } catch (Exception e) {
            log.error("Failed to send Kafka notification for order {}: {}", savedOrder.getId(), e.getMessage());
            // Don't fail the order creation if Kafka fails
        }

        return savedOrder;
    }


    private BigDecimal calculateTotal(List< OrderItemDTO > items) {
        return items.stream()
                .map(i -> i.getUnitPrice().multiply(new BigDecimal(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public Orders getOrderWithItems(Long orderId) {
        return ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException( String.valueOf( orderId ) ));

    }

}

