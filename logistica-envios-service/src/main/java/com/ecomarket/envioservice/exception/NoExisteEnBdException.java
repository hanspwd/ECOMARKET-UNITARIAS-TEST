package com.ecomarket.envioservice.exception;

public class NoExisteEnBdException extends RuntimeException{

    public NoExisteEnBdException(String mensaje) {
        super(mensaje);
    }
}
