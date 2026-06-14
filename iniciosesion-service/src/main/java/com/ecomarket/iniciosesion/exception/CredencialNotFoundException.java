package com.ecomarket.iniciosesion.exception;

public class CredencialNotFoundException extends RuntimeException {

    public CredencialNotFoundException(String mensaje) {
        super(mensaje);
    }
}
