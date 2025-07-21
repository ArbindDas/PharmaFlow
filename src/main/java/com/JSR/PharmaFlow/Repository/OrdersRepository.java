package com.JSR.PharmaFlow.Repository;

import com.JSR.PharmaFlow.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.JSR.PharmaFlow.Entity.Orders;

import java.util.List;

@Repository
public interface OrdersRepository extends  JpaRepository<Orders, Long> {
    List <Orders> findByUsers(Users user);
}
