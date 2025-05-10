package com.JSR.online_pharmacy_management.Entity;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table (
        name = "order_items"
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItmes {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    private  String quantity;

    private BigDecimal unitPrice;



    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Orders orders;


    @OneToMany(mappedBy = "orderItmes" , cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Medicines>medicinesList = new ArrayList <> (  );
}
