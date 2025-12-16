package com.JSR.PharmaFlow.Repository;
import com.JSR.PharmaFlow.Entity.Orders;
import com.JSR.PharmaFlow.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;



@Repository
public interface OrdersRepository extends JpaRepository < Orders, Long> {

    List<Orders> findByUsersId(Long userId);
    List <Orders> findByUsers(Users user);

    List< Orders> findByUsersIdOrderByCreatedAtDesc(Long userId);
//    List<Orders> findByUsersIdOrderByCreatedAtDesc(Long userId);
    List<Orders> findAllByOrderByCreatedAtDesc(); // Add this method
}