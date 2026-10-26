package com.trinity.banca.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ClienteFechasTest {

    @Autowired
    private TestEntityManager entityManager;

    private Cliente nuevoCliente() {
        Cliente cliente = new Cliente();
        cliente.setTipoIdentificacion(TipoIdentificacion.CC);
        cliente.setNumeroIdentificacion("1020304050");
        cliente.setNombres("Ana");
        cliente.setApellido("Pérez");
        cliente.setCorreoElectronico("ana@correo.com");
        cliente.setFechaNacimiento(LocalDate.now().minusYears(30));
        return cliente;
    }

    @Test
    void alGuardarSeAsignaFechaCreacionYModificacionQuedaNula() {
        LocalDateTime antesDeGuardar = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

        Cliente guardado = entityManager.persistFlushFind(nuevoCliente());

        assertNotNull(guardado.getId());
        assertNotNull(guardado.getFechaCreacion());
        assertFalse(guardado.getFechaCreacion().isBefore(antesDeGuardar));
        assertNull(guardado.getFechaModificacion());
    }

    @Test
    void alModificarSeAsignaFechaModificacionYCreacionNoCambia() {
        Cliente guardado = entityManager.persistFlushFind(nuevoCliente());
        LocalDateTime creacionOriginal = guardado.getFechaCreacion();

        guardado.setNombres("Ana María");
        entityManager.flush();
        entityManager.clear();

        Cliente modificado = entityManager.find(Cliente.class, guardado.getId());
        assertEquals("Ana María", modificado.getNombres());
        assertNotNull(modificado.getFechaModificacion());
        assertEquals(creacionOriginal, modificado.getFechaCreacion());
    }
}