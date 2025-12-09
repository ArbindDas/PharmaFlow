////package com.JSR.PharmaFlow.Entity;
////
////
////import java.math.BigDecimal;
////import java.util.ArrayList;
////import java.util.List;
////
////import com.fasterxml.jackson.annotation.JsonBackReference;
////import com.fasterxml.jackson.annotation.JsonManagedReference;
////
////import jakarta.persistence.CascadeType;
////import jakarta.persistence.Entity;
////import jakarta.persistence.GeneratedValue;
////import jakarta.persistence.GenerationType;
////import jakarta.persistence.Id;
////import jakarta.persistence.JoinColumn;
////import jakarta.persistence.ManyToOne;
////import jakarta.persistence.OneToMany;
////import jakarta.persistence.Table;
////import lombok.AllArgsConstructor;
////import lombok.Getter;
////import lombok.NoArgsConstructor;
////import lombok.Setter;
////import org.springframework.security.core.AuthenticatedPrincipal;
////
////@Entity
////@Table (
////        name = "order_items"
////)
////@Getter
////@Setter
////@AllArgsConstructor
////@NoArgsConstructor
////
////public class OrderItems {
////
////    @Id
////    @GeneratedValue(
////            strategy = GenerationType.IDENTITY
////    )
////    private Long id;
////
////
////    private  String quantity;
////
////    private BigDecimal unitPrice;
////
////
////    @OneToMany(mappedBy = "orderItems" , cascade = CascadeType.ALL)
////    @JsonManagedReference("orderitem-medicines")
////    private List<Medicines>medicinesList = new ArrayList <> (  );
////
////
////    @ManyToOne
////    @JoinColumn(name = "order_id", nullable = false)
////    @JsonBackReference("order-items")
////    private Orders orders;
////
////
////    public OrderItems(String quantity, BigDecimal unitPrice, Orders order) {
////        this.quantity = quantity;
////        this.unitPrice = unitPrice;
////        this.orders = order;
////    }
////
////    public AuthenticatedPrincipal getMedicine(){
////        return getMedicine();
////    }
////}
//
//
//package com.JSR.PharmaFlow.Entity;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.fasterxml.jackson.annotation.JsonManagedReference;
//
//import jakarta.persistence.CascadeType;
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//import jakarta.persistence.OneToMany;
//import jakarta.persistence.Table;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//@Entity
//@Table(name = "order_items")
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//
//public class OrderItems {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private Integer quantity;
//    private BigDecimal unitPrice;
//
//    @ManyToOne
//    @JoinColumn(name = "medicine_id")
//    private Medicines medicine;
//
//    @ManyToOne
//    @JoinColumn(name = "order_id", nullable = false)
//    @JsonBackReference("order-items")
//    private Orders orders;
//
//    public OrderItems(Integer quantity, BigDecimal unitPrice, Orders order) {
//        this.quantity = quantity;
//        this.unitPrice = unitPrice;
//        this.orders = order;
//    }
//}

package com.JSR.PharmaFlow.Entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    private BigDecimal unitPrice;

    @ManyToOne
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicines medicine;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference("order-items")
    private Orders orders;

    public OrderItems(Integer quantity, BigDecimal unitPrice, Medicines medicine, Orders order) {
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.medicine = medicine;
        this.orders = order;
    }

    public OrderItems(Integer quantity, BigDecimal unitPrice, Orders order) {

    }


    public void setTotalPrice(BigDecimal multiply) {

    }
}