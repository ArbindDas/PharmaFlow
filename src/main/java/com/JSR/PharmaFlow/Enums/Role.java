package com.JSR.PharmaFlow.Enums;


import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Getter;
public enum Role {
    USER,
    ADMIN,
    DELIVERY;
}