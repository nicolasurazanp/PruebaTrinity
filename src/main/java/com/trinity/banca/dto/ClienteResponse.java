package com.trinity.banca.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.trinity.banca.entity.Cliente;
import com.trinity.banca.entity.TipoIdentificacion;

public record ClienteResponse(
        Long id,
        TipoIdentificacion tipoIdentificacion,
        String numeroIdentificacion,
        String nombres,
        String apellido,
        String correoElectronico,
        LocalDate fechaNacimiento,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion) {

    public static ClienteResponse desde(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getTipoIdentificacion(),
                cliente.getNumeroIdentificacion(),
                cliente.getNombres(),
                cliente.getApellido(),
                cliente.getCorreoElectronico(),
                cliente.getFechaNacimiento(),
                cliente.getFechaCreacion(),
                cliente.getFechaModificacion());
    }
}