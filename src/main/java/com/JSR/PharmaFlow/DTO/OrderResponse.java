package com.JSR.PharmaFlow.DTO;

import com.JSR.PharmaFlow.Enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {

    private Long orderId;
    private BigDecimal totalPrice;
    private Status status;
    private Instant orderDate;
    private List<OrderItemDTO> items;
}