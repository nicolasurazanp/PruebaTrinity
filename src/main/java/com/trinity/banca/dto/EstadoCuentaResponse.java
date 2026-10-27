package com.trinity.banca.dto;

import java.math.BigDecimal;
import java.util.List;

import com.trinity.banca.entity.Cuenta;
import com.trinity.banca.entity.EstadoCuenta;
import com.trinity.banca.entity.Movimiento;
import com.trinity.banca.entity.TipoCuenta;

public record EstadoCuentaResponse(
        Long cuentaId,
        String numeroCuenta,
        TipoCuenta tipoCuenta,
        EstadoCuenta estado,
        BigDecimal saldo,
        BigDecimal saldoDisponible,
        List<MovimientoResponse> movimientos) {

    public static EstadoCuentaResponse desde(Cuenta cuenta, List<Movimiento> movimientos) {
        return new EstadoCuentaResponse(
                cuenta.getId(),
                cuenta.getNumeroCuenta(),
                cuenta.getTipoCuenta(),
                cuenta.getEstado(),
                cuenta.getSaldo(),
                cuenta.getSaldo(),
                movimientos.stream().map(MovimientoResponse::desde).toList());
    }
}
