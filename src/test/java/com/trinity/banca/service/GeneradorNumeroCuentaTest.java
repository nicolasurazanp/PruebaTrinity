package com.trinity.banca.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trinity.banca.entity.TipoCuenta;
import com.trinity.banca.repository.CuentaRepository;

@ExtendWith(MockitoExtension.class)
class GeneradorNumeroCuentaTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @InjectMocks
    private GeneradorNumeroCuenta generador;

    @Test
    void generaNumeroDeAhorrosQueEmpiezaPor53() {
        String numero = generador.generar(TipoCuenta.AHORROS);

        assertTrue(numero.matches("53[0-9]{8}"));
    }

    @Test
    void generaNumeroCorrienteQueEmpiezaPor33() {
        String numero = generador.generar(TipoCuenta.CORRIENTE);

        assertTrue(numero.matches("33[0-9]{8}"));
    }

    @Test
    void generaOtroNumeroSiElPrimeroYaExiste() {
        when(cuentaRepository.existsByNumeroCuenta(anyString())).thenReturn(true, false);

        String numero = generador.generar(TipoCuenta.AHORROS);

        assertTrue(numero.matches("53[0-9]{8}"));
        verify(cuentaRepository, times(2)).existsByNumeroCuenta(anyString());
    }
}