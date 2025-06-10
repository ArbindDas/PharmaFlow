package com.JSR.PharmaFlow.Services;

import com.JSR.PharmaFlow.DTO.MedicineBasicDto;
import com.JSR.PharmaFlow.Entity.Medicines;
import com.JSR.PharmaFlow.Entity.OrderItems;
import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Exception.InsufficientStockException;
import com.JSR.PharmaFlow.Repository.MedicinesRepository;
import com.JSR.PharmaFlow.Repository.OrdersItemsRepository;
import com.JSR.PharmaFlow.Repository.UsersRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class MedicinesService {

    @Autowired
    private MedicinesRepository medicinesRepository;

    @Autowired
    private UsersRepository usersRepository;

    private static final Logger logger = LoggerFactory.getLogger ( MedicinesService.class );



    @Autowired
    private  OrdersItemsRepository orderItemsRepository;


    public List< OrderItems > getOrderItemsForMedicine(Long medicineId) {
        Medicines medicine = medicinesRepository.findById(medicineId)
                .orElseThrow(() -> new EntityNotFoundException("Medicine not found"));
        return Collections.singletonList( medicine.getOrderItems() );
    }


    public OrderItems createOrderItemForMedicine(Long medicineId, OrderItems orderItem) {
        Medicines medicine = medicinesRepository.findById(medicineId)
                .orElseThrow(() -> new EntityNotFoundException("Medicine not found"));


        orderItem.setMedicinesList(List.of(medicine) );


        return orderItemsRepository.save(orderItem);
    }


    @Transactional
    public void updateStockAfterOrder(Long medicineId, Integer quantityOrdered) {
        Medicines medicine = medicinesRepository.findById(medicineId)
                .orElseThrow(() -> new EntityNotFoundException("Medicine not found"));

        if (medicine.getStock() < quantityOrdered) {
            throw new InsufficientStockException("Not enough stock available");
        }

        medicine.setStock(medicine.getStock() - quantityOrdered);
        medicinesRepository.save(medicine);
    }

    public Medicines createMedicine(MedicineBasicDto medicineDto, Long createdByUserId) {
        Users creator = usersRepository.findById(createdByUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));


        Medicines medicine = new Medicines();

        medicine.setName(medicineDto.name());



        medicine.setCreatedByUser(creator);


        creator.getCreatedMedicines().add(medicine);


        return medicinesRepository.save(medicine);
    }

    public void updateMedicineCreator(Long medicineId, Long newCreatorId) {
        Medicines medicine = medicinesRepository.findById(medicineId)
                .orElseThrow();
        Users newCreator = usersRepository.findById(newCreatorId)
                .orElseThrow();

        Users oldCreator = medicine.getCreatedByUser();
        if (oldCreator != null) {
            oldCreator.getCreatedMedicines().remove(medicine);
        }

        medicine.setCreatedByUser(newCreator);
        newCreator.getCreatedMedicines().add(medicine);

        medicinesRepository.save(medicine);
    }

    public Medicines getMedicineWithCreator(Long id) {
        return medicinesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Medicine not found"));
    }
}
