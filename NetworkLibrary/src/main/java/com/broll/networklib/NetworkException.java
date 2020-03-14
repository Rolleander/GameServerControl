package com.broll.networklib;

public class NetworkException extends RuntimeException {

    public NetworkException(String message){
        super(message);
    }

    public NetworkException(Throwable cause){
        super(cause);
    }

    public NetworkException( String message, Throwable cause){
        super(message,cause);
    }

}
