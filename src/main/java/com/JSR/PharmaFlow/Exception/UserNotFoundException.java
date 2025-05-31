package com.JSR.PharmaFlow.Exception;

public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException ( String message , String eMessage ){
        super(message);
    }
}
