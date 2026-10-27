package com.trinity.banca.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import com.trinity.banca.entity.Cliente;
import com.trinity.banca.entity.Cuenta;
import com.trinity.banca.entity.Movimiento;
import com.trinity.banca.entity.TipoCuenta;
import com.trinity.banca.entity.TipoIdentificacion;
import com.trinity.banca.entity.TipoMovimiento;
import com.trinity.banca.entity.TipoTransaccion;
import com.trinity.banca.exception.ReglaNegocioException;
import com.trinity.banca.service.CuentaService;
import com.trinity.banca.service.TransaccionService;

@WebMvcTest(TransaccionController.class)
class TransaccionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransaccionService transaccionService;

    @MockitoBean
    private CuentaService cuentaService;

    private Cuenta cuenta(Long id, String numero) {
        Cliente cliente = new Cliente(TipoIdentificacion.CC, "1020304050", "Ana", "Perez",
                "ana@correo.com", LocalDate.of(1995, 5, 20));
        Cuenta cuenta = new Cuenta(TipoCuenta.AHORROS, numero, false, cliente);
        ReflectionTestUtils.setField(cuenta, "id", id);
        return cuenta;
    }

    @Test
    void consignarDevuelve201ConElMovimiento() throws Exception {
        Cuenta cuenta = cuenta(1L, "5300000001");
        cuenta.acreditar(new BigDecimal("100.00"));
        Movimiento movimiento = new Movimiento("codigo-1", cuenta,
                TipoTransaccion.CONSIGNACION, TipoMovimiento.CREDITO, new BigDecimal("100.00"));
        when(transaccionService.consignar(1L, new BigDecimal("100.00"))).thenReturn(movimiento);

        mockMvc.perform(post("/api/transacciones/consignaciones").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cuentaId":1,"monto":100.00}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoMovimiento").value("CREDITO"))
                .andExpect(jsonPath("$.saldoResultante").value(100.00))
                .andExpect(jsonPath("$.cuentaId").value(1));
    }

    @Test
    void retirarConMontoNegativoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/transacciones/retiros").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cuentaId":1,"monto":-50}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.monto").exists());

        verify(transaccionService, never()).retirar(any(), any());
    }

    @Test
    void retirarConMasDeDosDecimalesDevuelve400() throws Exception {
        mockMvc.perform(post("/api/transacciones/retiros").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cuentaId":1,"monto":10.005}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.monto").exists());
    }

    @Test
    void retirarSinSaldoDevuelve422() throws Exception {
        when(transaccionService.retirar(1L, new BigDecimal("500")))
                .thenThrow(new ReglaNegocioException("Saldo insuficiente en la cuenta 5300000001"));

        mockMvc.perform(post("/api/transacciones/retiros").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cuentaId":1,"monto":500}
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Saldo insuficiente en la cuenta 5300000001"));
    }

    @Test
    void transferirDevuelve201ConDebitoYCredito() throws Exception {
        Cuenta origen = cuenta(1L, "5300000001");
        Cuenta destino = cuenta(2L, "3300000002");
        destino.acreditar(new BigDecimal("40.00"));
        Movimiento debito = new Movimiento("codigo-1", origen,
                TipoTransaccion.TRANSFERENCIA, TipoMovimiento.DEBITO, new BigDecimal("40.00"));
        Movimiento credito = new Movimiento("codigo-1", destino,
                TipoTransaccion.TRANSFERENCIA, TipoMovimiento.CREDITO, new BigDecimal("40.00"));
        when(transaccionService.transferir(1L, 2L, new BigDecimal("40.00"))).thenReturn(List.of(debito, credito));

        mockMvc.perform(post("/api/transacciones/transferencias").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cuentaOrigenId":1,"cuentaDestinoId":2,"monto":40.00}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tipoMovimiento").value("DEBITO"))
                .andExpect(jsonPath("$[0].cuentaId").value(1))
                .andExpect(jsonPath("$[1].tipoMovimiento").value("CREDITO"))
                .andExpect(jsonPath("$[1].cuentaId").value(2));
    }

    @Test
    void estadoDeCuentaDevuelveSaldoYMovimientos() throws Exception {
        Cuenta cuenta = cuenta(1L, "5300000001");
        cuenta.acreditar(new BigDecimal("100.00"));
        Movimiento movimiento = new Movimiento("codigo-1", cuenta,
                TipoTransaccion.CONSIGNACION, TipoMovimiento.CREDITO, new BigDecimal("100.00"));
        when(transaccionService.estadoDeCuenta(1L)).thenReturn(List.of(movimiento));
        when(cuentaService.obtenerPorId(1L)).thenReturn(cuenta);

        mockMvc.perform(get("/api/cuentas/1/estado-cuenta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroCuenta").value("5300000001"))
                .andExpect(jsonPath("$.saldo").value(100.00))
                .andExpect(jsonPath("$.saldoDisponible").value(100.00))
                .andExpect(jsonPath("$.movimientos.length()").value(1));
    }
}
