package com.JSR.PharmaFlow.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponse {

    private Long orderItemId;
    private Long medicineId;
    private String medicineName;
    private int quantity;
    private BigDecimal unitPrice;

    public OrderItemResponse(Long id , String name , String name1 , String quantity , BigDecimal unitPrice){
    }
}
