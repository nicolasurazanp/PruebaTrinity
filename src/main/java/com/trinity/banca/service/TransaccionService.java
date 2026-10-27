package com.trinity.banca.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trinity.banca.entity.Cuenta;
import com.trinity.banca.entity.Movimiento;
import com.trinity.banca.entity.TipoMovimiento;
import com.trinity.banca.entity.TipoTransaccion;
import com.trinity.banca.exception.ReglaNegocioException;
import com.trinity.banca.repository.MovimientoRepository;

@Service
public class TransaccionService {

    private final CuentaService cuentaService;
    private final MovimientoRepository movimientoRepository;

    public TransaccionService(CuentaService cuentaService, MovimientoRepository movimientoRepository) {
        this.cuentaService = cuentaService;
        this.movimientoRepository = movimientoRepository;
    }

    @Transactional
    public Movimiento consignar(Long cuentaId, BigDecimal monto) {
        Cuenta cuenta = cuentaService.obtenerPorId(cuentaId);
        cuenta.acreditar(monto);
        return movimientoRepository.save(new Movimiento(nuevoCodigo(), cuenta,
                TipoTransaccion.CONSIGNACION, TipoMovimiento.CREDITO, monto));
    }

    @Transactional
    public Movimiento retirar(Long cuentaId, BigDecimal monto) {
        Cuenta cuenta = cuentaService.obtenerPorId(cuentaId);
        cuenta.debitar(monto);
        return movimientoRepository.save(new Movimiento(nuevoCodigo(), cuenta,
                TipoTransaccion.RETIRO, TipoMovimiento.DEBITO, monto));
    }

    @Transactional
    public List<Movimiento> transferir(Long cuentaOrigenId, Long cuentaDestinoId, BigDecimal monto) {
        if (Objects.equals(cuentaOrigenId, cuentaDestinoId)) {
            throw new ReglaNegocioException("La cuenta de origen y la de destino deben ser distintas");
        }
        Cuenta origen = cuentaService.obtenerPorId(cuentaOrigenId);
        Cuenta destino = cuentaService.obtenerPorId(cuentaDestinoId);

        origen.debitar(monto);
        destino.acreditar(monto);

        String codigo = nuevoCodigo();
        Movimiento debito = new Movimiento(codigo, origen, TipoTransaccion.TRANSFERENCIA, TipoMovimiento.DEBITO, monto);
        Movimiento credito = new Movimiento(codigo, destino, TipoTransaccion.TRANSFERENCIA, TipoMovimiento.CREDITO, monto);
        return movimientoRepository.saveAll(List.of(debito, credito));
    }

    private String nuevoCodigo() {
        return UUID.randomUUID().toString();
    }
}
