package com.JSR.PharmaFlow.Repository;

import com.JSR.PharmaFlow.Entity.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemsRepository extends JpaRepository< OrderItems  , Long > {
}
