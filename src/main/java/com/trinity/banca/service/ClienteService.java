package com.trinity.banca.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trinity.banca.entity.Cliente;
import com.trinity.banca.entity.TipoIdentificacion;
import com.trinity.banca.exception.ReglaNegocioException;
import com.trinity.banca.repository.ClienteRepository;
import com.trinity.banca.validation.MayorDeEdad;
import com.trinity.banca.validation.MayorDeEdadValidator;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public Cliente crear(Cliente cliente) {
        validarMayorDeEdad(cliente.getFechaNacimiento());

        validarIdentificacionNoRegistrada(cliente.getTipoIdentificacion(),
                cliente.getNumeroIdentificacion());
        return clienteRepository.save(cliente);
    }

    private void validarMayorDeEdad(LocalDate fechaNacimiento) {
        if (fechaNacimiento != null && !MayorDeEdadValidator.esMayorDeEdad(fechaNacimiento,
                MayorDeEdad.EDAD_MINIMA)) {
            throw new ReglaNegocioException("El cliente debe ser mayor de edad");
        }
    }

   private void validarIdentificacionNoRegistrada(TipoIdentificacion tipo, String numero){
    if 
    (clienteRepository.existsByTipoIdentificacionAndNumeroIdentificacion(tipo, numero)){
        throw new ReglaNegocioException("Ya existe un cliente con " + tipo + " " + numero);
    }

   }
}
