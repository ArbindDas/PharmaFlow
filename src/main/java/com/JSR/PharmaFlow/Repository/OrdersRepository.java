package com.JSR.PharmaFlow.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.JSR.PharmaFlow.Entity.Orders;

@Repository
public interface OrdersRepository extends  JpaRepository<Orders, Long> {

}
