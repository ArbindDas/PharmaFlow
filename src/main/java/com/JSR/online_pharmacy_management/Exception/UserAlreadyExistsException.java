package com.JSR.online_pharmacy_management.Exception;

public class UserAlreadyExistsException extends RuntimeException
{
    public UserAlreadyExistsException (String message){
        super(message);
    }
    public UserAlreadyExistsException(String message , Throwable cause){
        super(message , cause);
    }
}
