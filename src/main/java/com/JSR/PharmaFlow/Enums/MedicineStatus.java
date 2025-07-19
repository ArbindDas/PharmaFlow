package com.JSR.PharmaFlow.Enums;

public enum MedicineStatus {
    ADDED,        // Just added to inventory
    AVAILABLE,    // Available for sale
    OUT_OF_STOCK, // Currently unavailable
    EXPIRED,      // Expired product
    DISCONTINUED; // No longer sold

    public String getDescription(){
        return switch(this){
            case ADDED -> "Medicine has been added to inventory";
            case AVAILABLE -> "Medicine is available for sale";
            case OUT_OF_STOCK -> "Medicine is out of stock";
            case EXPIRED -> "Medicine has expired";
            case DISCONTINUED -> "Medicine is discontinued";
        };
    }
}
