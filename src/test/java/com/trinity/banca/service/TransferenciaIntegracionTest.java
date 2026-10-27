package com.trinity.banca.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.trinity.banca.entity.Cliente;
import com.trinity.banca.entity.TipoCuenta;
import com.trinity.banca.entity.TipoIdentificacion;
import com.trinity.banca.exception.ReglaNegocioException;
import com.trinity.banca.repository.ClienteRepository;
import com.trinity.banca.repository.CuentaRepository;
import com.trinity.banca.repository.MovimientoRepository;

@SpringBootTest
class TransferenciaIntegracionTest {

    @Autowired
    private TransaccionService transaccionService;

    @Autowired
    private CuentaService cuentaService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    private Long clienteId;
    private Long origenId;
    private Long destinoId;

    @BeforeEach
    void prepararCuentas() {
        String identificacion = "IT-" + UUID.randomUUID().toString().substring(0, 8);
        Cliente cliente = clienteService.crear(new Cliente(TipoIdentificacion.CC, identificacion,
                "Prueba", "Integracion", "prueba@correo.com", LocalDate.of(1990, 1, 1)));
        clienteId = cliente.getId();
        origenId = cuentaService.crear(clienteId, TipoCuenta.AHORROS, false).getId();
        destinoId = cuentaService.crear(clienteId, TipoCuenta.CORRIENTE, false).getId();
        transaccionService.consignar(origenId, new BigDecimal("100.00"));
    }

    @AfterEach
    void limpiar() {
        movimientoRepository.deleteAll(movimientoRepository.findByCuentaIdOrderByFechaAscIdAsc(origenId));
        movimientoRepository.deleteAll(movimientoRepository.findByCuentaIdOrderByFechaAscIdAsc(destinoId));
        cuentaRepository.deleteAllById(List.of(origenId, destinoId));
        clienteRepository.deleteById(clienteId);
    }

    @Test
    void transferenciaExitosaActualizaAmbosSaldosEnLaBaseDeDatos() {
        transaccionService.transferir(origenId, destinoId, new BigDecimal("40.00"));

        assertEquals(0, cuentaService.obtenerPorId(origenId).getSaldo().compareTo(new BigDecimal("60.00")));
        assertEquals(0, cuentaService.obtenerPorId(destinoId).getSaldo().compareTo(new BigDecimal("40.00")));
        assertEquals(2, movimientoRepository.findByCuentaIdOrderByFechaAscIdAsc(origenId).size());
        assertEquals(1, movimientoRepository.findByCuentaIdOrderByFechaAscIdAsc(destinoId).size());
    }

    @Test
    void siElCreditoFallaElDebitoSeDeshace() {
        cuentaService.inactivar(destinoId);

        assertThrows(ReglaNegocioException.class,
                () -> transaccionService.transferir(origenId, destinoId, new BigDecimal("40.00")));

        assertEquals(0, cuentaService.obtenerPorId(origenId).getSaldo().compareTo(new BigDecimal("100.00")));
        assertEquals(1, movimientoRepository.findByCuentaIdOrderByFechaAscIdAsc(origenId).size());
        assertEquals(0, movimientoRepository.findByCuentaIdOrderByFechaAscIdAsc(destinoId).size());
    }
}
