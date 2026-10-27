package com.trinity.banca.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.trinity.banca.entity.Cliente;
import com.trinity.banca.entity.TipoIdentificacion;

class ClienteResponseTest {

    @Test
    void desdeCopiaLosDatosDelCliente() {
        LocalDate fechaNacimiento = LocalDate.of(1995, 5, 20);
        Cliente cliente = new Cliente(TipoIdentificacion.CC, "1020304050", "Ana", "Pérez",
                "ana@correo.com", fechaNacimiento);

        ClienteResponse response = ClienteResponse.desde(cliente);

        assertEquals(TipoIdentificacion.CC, response.tipoIdentificacion());
        assertEquals("1020304050", response.numeroIdentificacion());
        assertEquals("Ana", response.nombres());
        assertEquals("Pérez", response.apellido());
        assertEquals("ana@correo.com", response.correoElectronico());
        assertEquals(fechaNacimiento, response.fechaNacimiento());
        assertNull(response.id());
    }
}