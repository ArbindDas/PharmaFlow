package com.JSR.PharmaFlow.Services;

import com.JSR.PharmaFlow.DTO.OrderItemDTO;
import com.JSR.PharmaFlow.Entity.OrderItems;
import com.JSR.PharmaFlow.Entity.Orders;
import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Enums.Status;
import com.JSR.PharmaFlow.Repository.OrdersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class OrdersService {




    private static final Logger logger = LoggerFactory.getLogger(OrdersService.class);

    @Autowired
    private OrdersRepository ordersRepository;


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
                    itemDto.quantity(),
                    itemDto.unitPrice(),
                    order
            );
            order.setOrderItemsList(List.of(item));
        }

        return ordersRepository.save(order);
    }

    private BigDecimal calculateTotal(List< OrderItemDTO > items) {
        return items.stream()
                .map(i -> i.unitPrice().multiply(new BigDecimal(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public Orders getOrderWithItems(Long orderId) {
        return ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException( String.valueOf( orderId ) ));

    }

}
