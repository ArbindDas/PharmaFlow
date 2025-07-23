package com.JSR.PharmaFlow.DTO;


import com.JSR.PharmaFlow.Enums.Status;
import com.JSR.PharmaFlow.Repository.OrderItemsRepository;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailResponse {
    private Long orderId;
    private BigDecimal totalPrice;
    private Status status;
    private LocalDateTime orderDate;
    private List < OrderItemsRepository > items;

    public OrderDetailResponse(Long id , BigDecimal totalPrice , Status status , Instant createdAt , List< OrderItemResponse> reversed){
    }

    // constructor, getters, setters
}