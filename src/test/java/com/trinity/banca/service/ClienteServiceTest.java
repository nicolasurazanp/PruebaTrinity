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
}