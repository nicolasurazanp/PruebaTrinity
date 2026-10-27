package com.trinity.banca.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.trinity.banca.entity.Cuenta;
import com.trinity.banca.entity.EstadoCuenta;
import com.trinity.banca.entity.TipoCuenta;

public record CuentaResponse(
        Long id,
        TipoCuenta tipoCuenta,
        String numeroCuenta,
        EstadoCuenta estado,
        BigDecimal saldo,
        boolean exentaGmf,
        Long clienteId,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion) {

    public static CuentaResponse desde(Cuenta cuenta) {
        return new CuentaResponse(
                cuenta.getId(),
                cuenta.getTipoCuenta(),
                cuenta.getNumeroCuenta(),
                cuenta.getEstado(),
                cuenta.getSaldo(),
                cuenta.isExentaGmf(),
                cuenta.getCliente().getId(),
                cuenta.getFechaCreacion(),
                cuenta.getFechaModificacion());
    }
}