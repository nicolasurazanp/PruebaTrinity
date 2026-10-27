package com.trinity.banca.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.trinity.banca.entity.TipoIdentificacion;
import com.trinity.banca.exception.RecursoNoEncontradoException;
import com.trinity.banca.exception.ReglaNegocioException;
import com.trinity.banca.service.ClienteService;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    private static final String JSON_VALIDO = """
            {"tipoIdentificacion":"CC","numeroIdentificacion":"1020304050","nombres":"Ana",
             "apellido":"Perez","correoElectronico":"ana@correo.com","fechaNacimiento":"1995-05-20"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    private Cliente clienteGuardado(Long id) {
        Cliente cliente = new Cliente(TipoIdentificacion.CC, "1020304050", "Ana", "Perez",
                "ana@correo.com", LocalDate.of(1995, 5, 20));
        ReflectionTestUtils.setField(cliente, "id", id);
        return cliente;
    }

    @Test
    void crearDevuelve201ConElClienteCreado() throws Exception {
        when(clienteService.crear(any(Cliente.class))).thenReturn(clienteGuardado(1L));

        mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/clientes/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombres").value("Ana"));
    }

    @Test
    void crearDevuelve400SiLosDatosNoSonValidos() throws Exception {
        String jsonInvalido = JSON_VALIDO.replace("\"Ana\"", "\"A\"");

        mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombres").exists());

        verify(clienteService, never()).crear(any());
    }

    @Test
    void crearDevuelve422SiSeIncumpleUnaReglaDeNegocio() throws Exception {
        when(clienteService.crear(any(Cliente.class)))
                .thenThrow(new ReglaNegocioException("Ya existe un cliente con CC 1020304050"));

        mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Ya existe un cliente con CC 1020304050"));
    }

    @Test
    void listarDevuelve200ConTodosLosClientes() throws Exception {
        when(clienteService.listar()).thenReturn(List.of(clienteGuardado(1L), clienteGuardado(2L)));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void obtenerPorIdDevuelve200SiExiste() throws Exception {
        when(clienteService.obtenerPorId(1L)).thenReturn(clienteGuardado(1L));

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroIdentificacion").value("1020304050"));
    }

    @Test
    void obtenerPorIdDevuelve404SiNoExiste() throws Exception {
        when(clienteService.obtenerPorId(999L))
                .thenThrow(new RecursoNoEncontradoException("No existe un cliente con id 999"));

        mockMvc.perform(get("/api/clientes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe un cliente con id 999"));
    }

    @Test
    void actualizarDevuelve200ConLosDatosNuevos() throws Exception {
        when(clienteService.actualizar(eq(1L), any(Cliente.class))).thenReturn(clienteGuardado(1L));

        mockMvc.perform(put("/api/clientes/1").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void eliminarDevuelve204() throws Exception {
        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isNoContent());

        verify(clienteService).eliminar(1L);
    }
}