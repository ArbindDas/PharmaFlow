package com.JSR.online_pharmacy_management.Exception;

public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException ( String message , String eMessage ){
        super(message);
    }
}
