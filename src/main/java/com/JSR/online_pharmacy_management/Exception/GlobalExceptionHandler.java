package com.JSR.online_pharmacy_management.Exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?>HandleUserNotFoundException( UserNotFoundException userNotFoundException , WebRequest webRequest ){
        ErroResponse erroResponse = new ErroResponse (
                LocalDateTime.now (),
                HttpStatus.NOT_FOUND.value (),
                "User not found",
                userNotFoundException.getMessage (),
                webRequest.getDescription ( false )
        );
        return new ResponseEntity <> ( erroResponse , HttpStatus.NOT_FOUND );
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> handleUserAlreadyExistsException(UserAlreadyExistsException userAlreadyExistsException ){
        return new ResponseEntity <> ( userAlreadyExistsException.getMessage ( ) , HttpStatus.BAD_REQUEST );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
    }


}
