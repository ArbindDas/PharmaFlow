package com.JSR.PharmaFlow.Services;

import com.JSR.PharmaFlow.Entity.Medicines;
import com.JSR.PharmaFlow.Entity.OrderItems;
import com.JSR.PharmaFlow.Repository.MedicinesRepository;
import com.JSR.PharmaFlow.Repository.OrdersItemsRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrdersItemsService {

    private static final Logger logger = LoggerFactory.getLogger(OrdersItemsService.class);

    private final OrdersItemsRepository ordersItemsRepository;




    @Autowired
    private  MedicinesRepository medicineRepository;


    @Autowired
    public OrdersItemsService(OrdersItemsRepository ordersItemsRepository) {
        this.ordersItemsRepository = ordersItemsRepository;
    }


    public OrderItems createOrderItem(OrderItems orderItem) {
        logger.info("Creating new order item: {}", orderItem);
        return ordersItemsRepository.save(orderItem);
    }


    public List<OrderItems> getAllOrderItems() {
        logger.info("Fetching all order items");
        return ordersItemsRepository.findAll();
    }


    public Optional<OrderItems> getOrderItemById(Long id) {
        logger.info("Fetching order item with ID: {}", id);
        return ordersItemsRepository.findById(id);
    }


    public OrderItems updateOrderItem(Long id, OrderItems updatedItem) {
        logger.info("Updating order item with ID: {}", id);
        return ordersItemsRepository.findById(id)
                .map(item -> {
                    item.setQuantity(updatedItem.getQuantity());
                    item.setUnitPrice(updatedItem.getUnitPrice());

                    return ordersItemsRepository.save(item);
                })
                .orElseThrow(() -> new RuntimeException("Order item not found with id: " + id));
    }


    public void deleteOrderItem(Long id) {
        logger.info("Deleting order item with ID: {}", id);
        ordersItemsRepository.deleteById(id);
    }


    public List<OrderItems> getItemsByOrderId(Long orderId) {
        logger.info("Fetching all items for order ID: {}", orderId);
        return ordersItemsRepository.findByOrders_Id(orderId);
    }




    // Create a new order item
    public OrderItems createOrderItem(OrderItems orderItem, Long medicineId) {
        Medicines medicine = medicineRepository.findById(medicineId)
                .orElseThrow(() -> new EntityNotFoundException("Medicine not found"));


        orderItem.setMedicinesList(List.of(medicine));

        orderItem.setUnitPrice(medicine.getPrice());

        return ordersItemsRepository.save(orderItem);
    }


    public List<OrderItems> getOrderItemsByMedicine(Long medicineId) {
        return ordersItemsRepository.findByMedicineId(medicineId);
    }

}