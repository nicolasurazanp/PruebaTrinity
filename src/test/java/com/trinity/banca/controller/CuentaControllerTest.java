package com.trinity.banca.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.trinity.banca.entity.TipoCuenta;
import com.trinity.banca.entity.TipoIdentificacion;
import com.trinity.banca.exception.RecursoNoEncontradoException;
import com.trinity.banca.exception.ReglaNegocioException;
import com.trinity.banca.service.CuentaService;

@WebMvcTest(CuentaController.class)
class CuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CuentaService cuentaService;

    private Cuenta cuentaGuardada() {
        Cliente cliente = new Cliente(TipoIdentificacion.CC, "1020304050", "Ana", "Perez",
                "ana@correo.com", LocalDate.of(1995, 5, 20));
        ReflectionTestUtils.setField(cliente, "id", 7L);
        Cuenta cuenta = new Cuenta(TipoCuenta.AHORROS, "5300000001", false, cliente);
        ReflectionTestUtils.setField(cuenta, "id", 1L);
        return cuenta;
    }

    @Test
    void crearDevuelve201ConLaCuentaCreada() throws Exception {
        when(cuentaService.crear(7L, TipoCuenta.AHORROS, false)).thenReturn(cuentaGuardada());

        mockMvc.perform(post("/api/cuentas").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":7,"tipoCuenta":"AHORROS","exentaGmf":false}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/cuentas/1"))
                .andExpect(jsonPath("$.numeroCuenta").value("5300000001"))
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.clienteId").value(7));
    }

    @Test
    void crearDevuelve400SiFaltanCampos() throws Exception {
        mockMvc.perform(post("/api/cuentas").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":7}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.tipoCuenta").exists())
                .andExpect(jsonPath("$.errores.exentaGmf").exists());
    }

    @Test
    void crearDevuelve400SiElTipoDeCuentaNoExiste() throws Exception {
        mockMvc.perform(post("/api/cuentas").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":7,"tipoCuenta":"PLAZO_FIJO","exentaGmf":false}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("El cuerpo de la petición no tiene un formato válido"));
    }

    @Test
    void listarPorClienteDevuelveSusCuentas() throws Exception {
        when(cuentaService.listarPorCliente(7L)).thenReturn(List.of(cuentaGuardada()));

        mockMvc.perform(get("/api/cuentas").param("clienteId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void obtenerPorIdDevuelve404SiNoExiste() throws Exception {
        when(cuentaService.obtenerPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("No existe una cuenta con id 99"));

        mockMvc.perform(get("/api/cuentas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void inactivarDevuelveLaCuentaInactiva() throws Exception {
        Cuenta cuenta = cuentaGuardada();
        cuenta.inactivar();
        when(cuentaService.inactivar(1L)).thenReturn(cuenta);

        mockMvc.perform(patch("/api/cuentas/1/inactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVA"));
    }

    @Test
    void cancelarDevuelve422SiTieneSaldo() throws Exception {
        when(cuentaService.cancelar(1L))
                .thenThrow(new ReglaNegocioException("Solo se pueden cancelar cuentas con saldo igual a $0"));

        mockMvc.perform(patch("/api/cuentas/1/cancelar"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Solo se pueden cancelar cuentas con saldo igual a $0"));
    }
}
