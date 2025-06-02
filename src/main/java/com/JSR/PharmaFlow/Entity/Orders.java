package com.JSR.PharmaFlow.Entity;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.JSR.PharmaFlow.Enums.Status;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table (
        name = "orders"
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Orders {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "id" , nullable = false)
    private Long id;


    private BigDecimal totalPrice;


    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_at" , nullable = false)
    private Instant createdAt;


    @PrePersist
    public void prePersist(){
        if ( this.createdAt == null ){
            this.createdAt = Instant.now ();  // Set the current time if not already set
        }
    }



    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("order-items")
    private List< OrderItems > orderItemsList = new ArrayList<>();


    @OneToOne()
    @JoinColumn(name = "prescription_id" , nullable = false , unique = true)
    @JsonBackReference("prescription-orders") // Orders is the child / FK holder
    private Prescription prescription;


    @ManyToOne
    @JoinColumn(name = "user_id"  ,nullable = false)
    @JsonBackReference("user-orders")
    private Users users;



}
