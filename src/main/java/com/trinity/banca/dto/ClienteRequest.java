package com.trinity.banca.dto;

import java.time.LocalDate;

import com.trinity.banca.entity.Cliente;
import com.trinity.banca.entity.TipoIdentificacion;
import com.trinity.banca.validation.MayorDeEdad;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotNull TipoIdentificacion tipoIdentificacion,
        @NotBlank @Size(max = 20) String numeroIdentificacion,
        @NotBlank @Size(min = 2, max = 100) String nombres,
        @NotBlank @Size(min = 2, max = 100) String apellido,
        @NotBlank @Email(regexp = Cliente.PATRON_CORREO) @Size(max = 150) String correoElectronico,
        @NotNull @MayorDeEdad LocalDate fechaNacimiento) {

    public Cliente aEntidad() {
        return new Cliente(tipoIdentificacion, numeroIdentificacion, nombres, apellido,
                correoElectronico, fechaNacimiento);
    }
}