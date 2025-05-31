package com.JSR.PharmaFlow.Entity;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
/*
    user --> parent  it uses mappedBy()  JsonManagedReference
    order --> child its ref to the user its one to one or one to many many to one JsonBackReference
 */

import com.JSR.PharmaFlow.Enums.OAuthProvider;
import com.JSR.PharmaFlow.Enums.Role;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table (
        name = "users"
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Users {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "id", nullable = false)
    private Long id;

    @NotBlank (message = "Full name cannot be empty")
    @Size (min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z .]+$", message = "Full name can only contain alphabets, spaces, and dots")
    @Column (name = "full_name", nullable = false)
    private String fullName;

    @NotBlank (message = "Email cannot be empty")
    @Email (message = "Email must be a valid email address")
    @Column (name = "email", nullable = false , unique = true)
    private String email;

    @NotBlank (message = "Password cannot be empty")
    @Pattern (
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$",
            message = "Password must be at least 8 characters long, include at least one uppercase letter, one lowercase letter, one number, and one special character."
    )
    @Column (name = "password", nullable = false)
    private String password;


    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @NotNull(message = "Role cannot be null")
    private Set<Role> roles;


    @Column (name = "auth_provider", nullable = false)
    @Enumerated (EnumType.STRING)
    private OAuthProvider authProvider;

    @Column (name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist ( ) {
        if ( this.createdAt == null ) {
            this.createdAt = Instant.now ( );  // Set the current time if not already set
        }
    }

    @OneToMany (mappedBy = "users", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("user-orders")
    private List < Orders > ordersList = new ArrayList <> ( );
    


    @JsonManagedReference("user-prescriptions")
    @OneToMany (mappedBy = "users", cascade = CascadeType.ALL)
    private List < Prescription > prescriptionList = new ArrayList <> ( );

    @OneToMany(mappedBy = "createdByUser")
    @JsonManagedReference("user-created-medicines")
    private List<Medicines> createdMedicines = new ArrayList<>();


}

