package com.trinity.banca.entity;

public enum TipoCuenta {
    AHORROS("53"),
    CORRIENTE("33");

    private final String prefijo;

    TipoCuenta(String prefijo){
        this.prefijo = prefijo;
    }
    public String getPrefijo(){
        return prefijo;
    }
}
