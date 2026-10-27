package com.trinity.banca.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ClienteValidacionTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void iniciarValidador() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void cerrarValidador() {
        factory.close();
    }

    private Cliente clienteValido() {
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
    void clienteValidoNoTieneErrores() {
        Set<ConstraintViolation<Cliente>> errores = validator.validate(clienteValido());

        assertTrue(errores.isEmpty());
    }

    @Test
    void clienteQueCumple18AniosHoyEsValido() {
        Cliente cliente = clienteValido();
        cliente.setFechaNacimiento(LocalDate.now().minusYears(18));

        Set<ConstraintViolation<Cliente>> errores = validator.validate(cliente);

        assertTrue(errores.isEmpty());
    }

    @Test
    void clienteAlQueLeFaltaUnDiaPara18AniosEsRechazado() {
        Cliente cliente = clienteValido();
        cliente.setFechaNacimiento(LocalDate.now().minusYears(18).plusDays(1));

        Set<ConstraintViolation<Cliente>> errores = validator.validate(cliente);

        assertEquals(1, errores.size());
        ConstraintViolation<Cliente> error = errores.iterator().next();
        assertEquals("fechaNacimiento", error.getPropertyPath().toString());
        assertEquals("El cliente debe ser mayor de edad", error.getMessage());
    }

    @Test
    void fechaNacimientoNulaSoloLaRechazaNotNull() {
        Cliente cliente = clienteValido();
        cliente.setFechaNacimiento(null);

        Set<ConstraintViolation<Cliente>> errores = validator.validate(cliente);

        assertEquals(1, errores.size());
        assertEquals("fechaNacimiento", errores.iterator().next().getPropertyPath().toString());
    }
}