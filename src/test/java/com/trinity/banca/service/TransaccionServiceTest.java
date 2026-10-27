package com.trinity.banca.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trinity.banca.entity.Cliente;
import com.trinity.banca.entity.Cuenta;
import com.trinity.banca.entity.Movimiento;
import com.trinity.banca.entity.TipoCuenta;
import com.trinity.banca.entity.TipoIdentificacion;
import com.trinity.banca.entity.TipoMovimiento;
import com.trinity.banca.entity.TipoTransaccion;
import com.trinity.banca.exception.ReglaNegocioException;
import com.trinity.banca.repository.MovimientoRepository;

@ExtendWith(MockitoExtension.class)
class TransaccionServiceTest {

    @Mock
    private CuentaService cuentaService;

    @Mock
    private MovimientoRepository movimientoRepository;

    @InjectMocks
    private TransaccionService transaccionService;

    private Cuenta cuentaConSaldo(String numero, String saldo) {
        Cliente cliente = new Cliente(TipoIdentificacion.CC, "1020304050", "Ana", "Perez",
                "ana@correo.com", LocalDate.of(1995, 5, 20));
        Cuenta cuenta = new Cuenta(TipoCuenta.AHORROS, numero, false, cliente);
        BigDecimal saldoInicial = new BigDecimal(saldo);
        if (saldoInicial.signum() > 0) {
            cuenta.acreditar(saldoInicial);
        }
        return cuenta;
    }

    @Test
    void consignarSumaElMontoYGuardaUnCredito() {
        Cuenta cuenta = cuentaConSaldo("5300000001", "0");
        when(cuentaService.obtenerPorId(1L)).thenReturn(cuenta);
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Movimiento movimiento = transaccionService.consignar(1L, new BigDecimal("100.00"));

        assertEquals(0, cuenta.getSaldo().compareTo(new BigDecimal("100.00")));
        assertEquals(TipoTransaccion.CONSIGNACION, movimiento.getTipoTransaccion());
        assertEquals(TipoMovimiento.CREDITO, movimiento.getTipoMovimiento());
        assertEquals(0, movimiento.getSaldoResultante().compareTo(new BigDecimal("100.00")));
    }

    @Test
    void retirarRestaElMontoYGuardaUnDebito() {
        Cuenta cuenta = cuentaConSaldo("5300000001", "100.00");
        when(cuentaService.obtenerPorId(1L)).thenReturn(cuenta);
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Movimiento movimiento = transaccionService.retirar(1L, new BigDecimal("30.00"));

        assertEquals(0, cuenta.getSaldo().compareTo(new BigDecimal("70.00")));
        assertEquals(TipoMovimiento.DEBITO, movimiento.getTipoMovimiento());
        assertEquals(0, movimiento.getSaldoResultante().compareTo(new BigDecimal("70.00")));
    }

    @Test
    void retirarSinSaldoSuficienteNoGuardaNingunMovimiento() {
        Cuenta cuenta = cuentaConSaldo("5300000001", "20.00");
        when(cuentaService.obtenerPorId(1L)).thenReturn(cuenta);

        assertThrows(ReglaNegocioException.class,
                () -> transaccionService.retirar(1L, new BigDecimal("50.00")));

        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void transferirMueveElDineroYGeneraDebitoYCredito() {
        Cuenta origen = cuentaConSaldo("5300000001", "100.00");
        Cuenta destino = cuentaConSaldo("3300000002", "0");
        when(cuentaService.obtenerPorId(1L)).thenReturn(origen);
        when(cuentaService.obtenerPorId(2L)).thenReturn(destino);
        when(movimientoRepository.saveAll(anyList())).thenAnswer(invocacion -> invocacion.getArgument(0));

        List<Movimiento> movimientos = transaccionService.transferir(1L, 2L, new BigDecimal("40.00"));

        assertEquals(0, origen.getSaldo().compareTo(new BigDecimal("60.00")));
        assertEquals(0, destino.getSaldo().compareTo(new BigDecimal("40.00")));

        Movimiento debito = movimientos.get(0);
        Movimiento credito = movimientos.get(1);
        assertSame(origen, debito.getCuenta());
        assertEquals(TipoMovimiento.DEBITO, debito.getTipoMovimiento());
        assertSame(destino, credito.getCuenta());
        assertEquals(TipoMovimiento.CREDITO, credito.getTipoMovimiento());
        assertEquals(debito.getCodigoTransaccion(), credito.getCodigoTransaccion());
    }

    @Test
    void transferirALaMismaCuentaFalla() {
        assertThrows(ReglaNegocioException.class,
                () -> transaccionService.transferir(1L, 1L, new BigDecimal("10.00")));

        verifyNoInteractions(cuentaService, movimientoRepository);
    }

    @Test
    void transferirAUnaCuentaInactivaNoGuardaMovimientos() {
        Cuenta origen = cuentaConSaldo("5300000001", "100.00");
        Cuenta destino = cuentaConSaldo("3300000002", "0");
        destino.inactivar();
        when(cuentaService.obtenerPorId(1L)).thenReturn(origen);
        when(cuentaService.obtenerPorId(2L)).thenReturn(destino);

        assertThrows(ReglaNegocioException.class,
                () -> transaccionService.transferir(1L, 2L, new BigDecimal("40.00")));

        verify(movimientoRepository, never()).saveAll(anyList());
    }
}
