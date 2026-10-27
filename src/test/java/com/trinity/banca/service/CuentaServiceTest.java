package com.trinity.banca.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.trinity.banca.entity.Cliente;
import com.trinity.banca.entity.Cuenta;
import com.trinity.banca.entity.EstadoCuenta;
import com.trinity.banca.entity.TipoCuenta;
import com.trinity.banca.entity.TipoIdentificacion;
import com.trinity.banca.exception.RecursoNoEncontradoException;
import com.trinity.banca.exception.ReglaNegocioException;
import com.trinity.banca.repository.CuentaRepository;

@ExtendWith(MockitoExtension.class)
class CuentaServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private GeneradorNumeroCuenta generadorNumeroCuenta;

    @InjectMocks
    private CuentaService cuentaService;

    private Cliente cliente() {
        return new Cliente(TipoIdentificacion.CC, "1020304050", "Ana", "Perez",
                "ana@correo.com", LocalDate.of(1995, 5, 20));
    }

    private Cuenta cuentaDeAhorros() {
        return new Cuenta(TipoCuenta.AHORROS, "5300000001", false, cliente());
    }

    @Test
    void crearGuardaCuentaActivaConNumeroGenerado() {
        Cliente cliente = cliente();
        when(clienteService.obtenerPorId(1L)).thenReturn(cliente);
        when(generadorNumeroCuenta.generar(TipoCuenta.AHORROS)).thenReturn("5300000001");
        when(cuentaRepository.save(any(Cuenta.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Cuenta cuenta = cuentaService.crear(1L, TipoCuenta.AHORROS, true);

        assertEquals("5300000001", cuenta.getNumeroCuenta());
        assertEquals(EstadoCuenta.ACTIVA, cuenta.getEstado());
        assertEquals(0, cuenta.getSaldo().compareTo(BigDecimal.ZERO));
        assertTrue(cuenta.isExentaGmf());
        assertSame(cliente, cuenta.getCliente());
    }

    @Test
    void crearFallaSiElClienteNoExiste() {
        when(clienteService.obtenerPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("No existe un cliente con id 99"));

        assertThrows(RecursoNoEncontradoException.class,
                () -> cuentaService.crear(99L, TipoCuenta.AHORROS, false));

        verifyNoInteractions(generadorNumeroCuenta, cuentaRepository);
    }

    @Test
    void obtenerPorIdLanzaExcepcionSiNoExiste() {
        when(cuentaRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException error = assertThrows(RecursoNoEncontradoException.class,
                () -> cuentaService.obtenerPorId(99L));

        assertEquals("No existe una cuenta con id 99", error.getMessage());
    }

    @Test
    void listarPorClienteDevuelveSusCuentas() {
        when(clienteService.obtenerPorId(1L)).thenReturn(cliente());
        when(cuentaRepository.findByClienteId(1L)).thenReturn(List.of(cuentaDeAhorros()));

        List<Cuenta> cuentas = cuentaService.listarPorCliente(1L);

        assertEquals(1, cuentas.size());
    }

    @Test
    void activarDejaLaCuentaActiva() {
        Cuenta cuenta = cuentaDeAhorros();
        cuenta.inactivar();
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));

        Cuenta resultado = cuentaService.activar(1L);

        assertEquals(EstadoCuenta.ACTIVA, resultado.getEstado());
    }

    @Test
    void inactivarDejaLaCuentaInactiva() {
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaDeAhorros()));

        Cuenta resultado = cuentaService.inactivar(1L);

        assertEquals(EstadoCuenta.INACTIVA, resultado.getEstado());
    }

    @Test
    void cancelarFallaSiLaCuentaTieneSaldo() {
        Cuenta cuenta = cuentaDeAhorros();
        ReflectionTestUtils.setField(cuenta, "saldo", new BigDecimal("50.00"));
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuenta));

        assertThrows(ReglaNegocioException.class, () -> cuentaService.cancelar(1L));

        assertEquals(EstadoCuenta.ACTIVA, cuenta.getEstado());
    }
}