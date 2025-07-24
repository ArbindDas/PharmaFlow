package com.JSR.PharmaFlow.Repository;//package com.JSR.PharmaFlow.Repository;
//
//import com.JSR.PharmaFlow.Entity.Users;
//import org.springframework.data.jpa.repository.EntityGraph;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import com.JSR.PharmaFlow.Entity.Orders;
//

import com.JSR.PharmaFlow.Entity.Orders;
import com.JSR.PharmaFlow.Entity.Users;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

////import java.lang.ScopedValue;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface OrdersRepository extends  JpaRepository<Orders, Long> {
//    List <Orders> findByUsers(Users user);
//    @EntityGraph (attributePaths = {"user", "orderItems", "orderItems.medicine"})
//    Optional <Orders> findByIdWithItems(Long id);
//}

@Repository
public interface OrdersRepository extends JpaRepository < Orders, Long> {
    List <Orders> findByUsers(Users user);

    List< Orders> findByUsersIdOrderByCreatedAtDesc(Long userId);

//    @EntityGraph (attributePaths = {"user", "orderItems", "orderItems.medicine"})
//    @Query ("SELECT o FROM Orders o WHERE o.id = :id")
//    Optional <Orders> findByIdWithItems(@Param ("id") Long id);
}