package com.ecomarket.catalogoinventarioservice.exception;

public class YaExisteEnBdException extends RuntimeException {

    public YaExisteEnBdException(String mensaje) {
        super(mensaje);
    }
}
