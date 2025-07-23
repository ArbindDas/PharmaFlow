package com.JSR.PharmaFlow.DTO;


import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemResponse {

    private Long orderItemId;
    private Long medicineId;
    private String medicineName;
    private int quantity;
    private BigDecimal unitPrice;

    public OrderItemResponse(Long id , String name , String name1 , String quantity , BigDecimal unitPrice){
    }
}
