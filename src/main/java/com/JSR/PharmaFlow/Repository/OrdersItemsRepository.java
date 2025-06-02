package com.JSR.PharmaFlow.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.JSR.PharmaFlow.Entity.OrderItems;

import java.util.List;

@Repository
public interface OrdersItemsRepository extends JpaRepository< OrderItems, Long> {
 
    List< OrderItems> findByMedicineId(Long medicineId);

    List< OrderItems> findByOrdersId(Long orderId);
}
