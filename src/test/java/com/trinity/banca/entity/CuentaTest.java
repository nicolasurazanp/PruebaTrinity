package com.trinity.banca.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.trinity.banca.exception.ReglaNegocioException;

class CuentaTest {

    private Cuenta nuevaCuenta() {
        Cliente cliente = new Cliente(TipoIdentificacion.CC, "1020304050", "Ana", "Perez",
                "ana@correo.com", LocalDate.of(1995, 5, 20));
        return new Cuenta(TipoCuenta.AHORROS, "5300000001", false, cliente);
    }

    @Test
    void nuevaCuentaEstaActivaYConSaldoCero() {
        Cuenta cuenta = nuevaCuenta();

        assertEquals(EstadoCuenta.ACTIVA, cuenta.getEstado());
        assertEquals(0, cuenta.getSaldo().compareTo(BigDecimal.ZERO));
    }

    @Test
    void sePuedeInactivarYVolverAActivar() {
        Cuenta cuenta = nuevaCuenta();

        cuenta.inactivar();
        assertEquals(EstadoCuenta.INACTIVA, cuenta.getEstado());

        cuenta.activar();
        assertEquals(EstadoCuenta.ACTIVA, cuenta.getEstado());
    }

    @Test
    void cancelarFuncionaConSaldoCero() {
        Cuenta cuenta = nuevaCuenta();
        ReflectionTestUtils.setField(cuenta, "saldo", new BigDecimal("0.00"));

        cuenta.cancelar();

        assertEquals(EstadoCuenta.CANCELADA, cuenta.getEstado());
    }

    @Test
    void cancelarFallaSiHaySaldo() {
        Cuenta cuenta = nuevaCuenta();
        ReflectionTestUtils.setField(cuenta, "saldo", new BigDecimal("100.00"));

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class, cuenta::cancelar);

        assertEquals("Solo se pueden cancelar cuentas con saldo igual a $0", error.getMessage());
        assertEquals(EstadoCuenta.ACTIVA, cuenta.getEstado());
    }

    @Test
    void unaCuentaCanceladaNoSePuedeActivar() {
        Cuenta cuenta = nuevaCuenta();
        cuenta.cancelar();

        assertThrows(ReglaNegocioException.class, cuenta::activar);
    }

    @Test
    void acreditarSumaElMontoAlSaldo() {
        Cuenta cuenta = nuevaCuenta();

        cuenta.acreditar(new BigDecimal("100.00"));

        assertEquals(0, cuenta.getSaldo().compareTo(new BigDecimal("100.00")));
    }

    @Test
    void debitarRestaElMontoDelSaldo() {
        Cuenta cuenta = nuevaCuenta();
        cuenta.acreditar(new BigDecimal("100.00"));

        cuenta.debitar(new BigDecimal("30.00"));

        assertEquals(0, cuenta.getSaldo().compareTo(new BigDecimal("70.00")));
    }

    @Test
    void debitarTodoElSaldoDejaLaCuentaEnCero() {
        Cuenta cuenta = nuevaCuenta();
        cuenta.acreditar(new BigDecimal("100.00"));

        cuenta.debitar(new BigDecimal("100.00"));

        assertEquals(0, cuenta.getSaldo().compareTo(BigDecimal.ZERO));
    }

    @Test
    void debitarFallaSiElSaldoQuedariaNegativo() {
        Cuenta cuenta = nuevaCuenta();
        cuenta.acreditar(new BigDecimal("100.00"));

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> cuenta.debitar(new BigDecimal("100.01")));

        assertEquals("Saldo insuficiente en la cuenta 5300000001", error.getMessage());
        assertEquals(0, cuenta.getSaldo().compareTo(new BigDecimal("100.00")));
    }

    @Test
    void noSePuedeOperarUnaCuentaInactiva() {
        Cuenta cuenta = nuevaCuenta();
        cuenta.inactivar();

        assertThrows(ReglaNegocioException.class, () -> cuenta.acreditar(new BigDecimal("10.00")));
        assertThrows(ReglaNegocioException.class, () -> cuenta.debitar(new BigDecimal("10.00")));
    }

    @Test
    void elMontoDebeSerMayorQueCero() {
        Cuenta cuenta = nuevaCuenta();

        assertThrows(ReglaNegocioException.class, () -> cuenta.acreditar(BigDecimal.ZERO));
        assertThrows(ReglaNegocioException.class, () -> cuenta.acreditar(new BigDecimal("-5.00")));
        assertThrows(ReglaNegocioException.class, () -> cuenta.acreditar(null));
    }
}