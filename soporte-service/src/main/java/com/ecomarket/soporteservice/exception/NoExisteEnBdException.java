package com.ecomarket.soporteservice.exception;

public class NoExisteEnBdException extends RuntimeException{
    
    public NoExisteEnBdException(String mensaje) {
        super(mensaje);
    }

}
