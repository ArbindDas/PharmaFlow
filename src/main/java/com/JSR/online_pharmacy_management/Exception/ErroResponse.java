package com.JSR.online_pharmacy_management.Exception;


import lombok.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ErroResponse {
    private LocalDateTime localDateTime;
    private int status;
    private String error;
    private String message;
    private  String path;
}
