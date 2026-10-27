package com.trinity.banca.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.trinity.banca.entity.Movimiento;
import com.trinity.banca.entity.TipoMovimiento;
import com.trinity.banca.entity.TipoTransaccion;

public record MovimientoResponse(
        Long id,
        String codigoTransaccion,
        Long cuentaId,
        TipoTransaccion tipoTransaccion,
        TipoMovimiento tipoMovimiento,
        BigDecimal monto,
        BigDecimal saldoResultante,
        LocalDateTime fecha) {

    public static MovimientoResponse desde(Movimiento movimiento) {
        return new MovimientoResponse(
                movimiento.getId(),
                movimiento.getCodigoTransaccion(),
                movimiento.getCuenta().getId(),
                movimiento.getTipoTransaccion(),
                movimiento.getTipoMovimiento(),
                movimiento.getMonto(),
                movimiento.getSaldoResultante(),
                movimiento.getFecha());
    }
}
