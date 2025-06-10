package com.JSR.PharmaFlow.Repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.JSR.PharmaFlow.Entity.OrderItems;

import java.util.List;

@Repository
public interface OrdersItemsRepository extends JpaRepository< OrderItems, Long> {
    // Correct if OrderItems has 'orders' field
    List<OrderItems> findByOrders_Id(Long orderId);
    // Custom query to find OrderItems by Medicine ID
    @Query ("SELECT oi FROM OrderItems oi JOIN oi.medicinesList m WHERE m.id = :medicineId")
    List<OrderItems> findByMedicineId(@Param ("medicineId") Long medicineId);
}

