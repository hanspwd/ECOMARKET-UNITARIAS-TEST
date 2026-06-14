package com.ecomarket.catalogoinventarioservice.exception;

public class NoExisteEnBdException extends RuntimeException {

    public NoExisteEnBdException(String mensaje) {
        super(mensaje);
    }
}
