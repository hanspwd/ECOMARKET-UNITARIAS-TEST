package com.ecomarket.soporteservice.exception;

public class YaExisteEnBdException extends RuntimeException{
    
    public YaExisteEnBdException(String mensaje) {
        super(mensaje);
    }

}