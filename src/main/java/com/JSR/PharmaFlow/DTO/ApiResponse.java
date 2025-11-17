package com.JSR.PharmaFlow.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {
    private String message;
    private String type; // "success" or "error"



    // Helper methods
    public static ApiResponse success(String message) {
        return new ApiResponse(message, "success");
    }

    public static ApiResponse error(String message) {
        return new ApiResponse(message, "error");
    }
}
