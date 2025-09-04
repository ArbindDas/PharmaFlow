package com.JSR.PharmaFlow.Repository;

import com.JSR.PharmaFlow.Entity.OrderItems;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface OrderItemsRepository extends JpaRepository< OrderItems  , Long > {

    @Modifying
    @Query("DELETE FROM OrderItems oi WHERE oi.medicine.id = :medicineId")
    void deleteByMedicineId(@Param("medicineId") Long medicineId);
}
