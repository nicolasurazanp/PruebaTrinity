package com.trinity.banca.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import com.trinity.banca.entity.TipoCuenta;
import com.trinity.banca.repository.CuentaRepository;

@Component
public class GeneradorNumeroCuenta {
    private static final int LIMITE_PARTE_ALETORIA = 100_000_000;

    private final CuentaRepository cuentaRepository;
    private final SecureRandom aletorio = new SecureRandom();

    public GeneradorNumeroCuenta(CuentaRepository cuentaRepository){
        this.cuentaRepository = cuentaRepository;
    }

    public String generar(TipoCuenta tipocuenta){
        String numero;
        do{
            int parteAleatoria=
            aletorio.nextInt(LIMITE_PARTE_ALETORIA);
            numero = tipocuenta.getPrefijo() + String.format("%08d", parteAleatoria);
    } while(cuentaRepository.existsByNumeroCuenta(numero));
    return numero;
    }
}
