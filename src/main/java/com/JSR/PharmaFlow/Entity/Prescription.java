package com.JSR.PharmaFlow.Entity;

import com.JSR.PharmaFlow.Enums.Status;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Entity
@Table (
        name = "prescription"
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Prescription {


    @Id
    @GeneratedValue (
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    @Enumerated (EnumType.STRING)
    private Status status;


    @Column (name = "file_url", nullable = false)
    @NotBlank (message = "File URL cannot be empty")
    @Size (max = 2048, message = "File URL is too long")
    private String fileUrl;


    private Instant createdAt;

    @PrePersist
    public void PrePersist ( ) {
        if ( this.createdAt == null ) {
            this.createdAt = Instant.now ( );
        }
    }


    @OneToOne(mappedBy = "prescription" , cascade = CascadeType.ALL)
    @JsonManagedReference("prescription-orders") // Prescription is the parent
    private Orders orders;



    @ManyToOne ()
    @JoinColumn (name = "user_id", nullable = false)
    @JsonBackReference("user-prescriptions")
    private Users users;


}

