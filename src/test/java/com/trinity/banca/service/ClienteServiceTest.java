package com.trinity.banca.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trinity.banca.entity.Cliente;
import com.trinity.banca.entity.TipoIdentificacion;
import com.trinity.banca.exception.ReglaNegocioException;
import com.trinity.banca.repository.ClienteRepository;
import java.util.List;
import java.util.Optional;
import com.trinity.banca.exception.RecursoNoEncontradoException;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente clienteConEdad(int anios) {
        return new Cliente(TipoIdentificacion.CC, "1020304050", "Ana", "Pérez",
                "ana@correo.com", LocalDate.now().minusYears(anios));
    }

    @Test
    void crearGuardaClienteMayorDeEdadConIdentificacionNueva() {
        Cliente cliente = clienteConEdad(30);
        when(clienteRepository.existsByTipoIdentificacionAndNumeroIdentificacion(TipoIdentificacion.CC, "1020304050"))
                .thenReturn(false);
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        Cliente resultado = clienteService.crear(cliente);

        assertSame(cliente, resultado);
        verify(clienteRepository).save(cliente);
    }

    @Test
    void crearRechazaClienteMenorDeEdad() {
        Cliente cliente = clienteConEdad(17);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> clienteService.crear(cliente));

        assertEquals("El cliente debe ser mayor de edad", error.getMessage());
        verifyNoInteractions(clienteRepository);
    }

    @Test
    void crearRechazaIdentificacionDuplicada() {
        Cliente cliente = clienteConEdad(30);
        when(clienteRepository.existsByTipoIdentificacionAndNumeroIdentificacion(TipoIdentificacion.CC, "1020304050"))
                .thenReturn(true);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> clienteService.crear(cliente));

        assertEquals("Ya existe un cliente con CC 1020304050", error.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void obtenerPorIdDevuelveClienteExistente() {
        Cliente cliente = clienteConEdad(30);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        Cliente resultado = clienteService.obtenerPorId(1L);

        assertSame(cliente, resultado);
    }

    @Test
    void obtenerPorIdLanzaExcepcionSiNoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException error = assertThrows(RecursoNoEncontradoException.class,
                () -> clienteService.obtenerPorId(99L));

        assertEquals("No existe un cliente con id 99", error.getMessage());
    }

    @Test
    void listarDevuelveTodosLosClientes() {
        List<Cliente> clientes = List.of(clienteConEdad(30), clienteConEdad(40));
        when(clienteRepository.findAll()).thenReturn(clientes);

        List<Cliente> resultado = clienteService.listar();

        assertEquals(2, resultado.size());
    }

    @Test
    void actualizarModificaLosDatosDelCliente() {
        Cliente existente = clienteConEdad(30);
        Cliente datos = new Cliente(TipoIdentificacion.CC, "1020304050", "Ana María", "Gómez",
                "ana.maria@correo.com", LocalDate.now().minusYears(31));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));

        Cliente resultado = clienteService.actualizar(1L, datos);

        assertSame(existente, resultado);
        assertEquals("Ana María", resultado.getNombres());
        assertEquals("Gómez", resultado.getApellido());
        assertEquals("ana.maria@correo.com", resultado.getCorreoElectronico());
        verify(clienteRepository, never()).existsByTipoIdentificacionAndNumeroIdentificacion(any(), any());
    }

    @Test
    void actualizarRechazaIdentificacionDeOtroCliente() {
        Cliente existente = clienteConEdad(30);
        Cliente datos = new Cliente(TipoIdentificacion.CC, "9999999999", "Ana", "Pérez",
                "ana@correo.com", LocalDate.now().minusYears(30));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(clienteRepository.existsByTipoIdentificacionAndNumeroIdentificacion(TipoIdentificacion.CC, "9999999999"))
                .thenReturn(true);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> clienteService.actualizar(1L, datos));

        assertEquals("Ya existe un cliente con CC 9999999999", error.getMessage());
        assertEquals("1020304050", existente.getNumeroIdentificacion());
    }

    @Test
    void actualizarRechazaClienteMenorDeEdad() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteConEdad(30)));

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> clienteService.actualizar(1L, clienteConEdad(17)));

        assertEquals("El cliente debe ser mayor de edad", error.getMessage());
    }
}