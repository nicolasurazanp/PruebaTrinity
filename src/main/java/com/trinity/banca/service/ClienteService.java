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
import java.util.List;
import com.trinity.banca.exception.RecursoNoEncontradoException;

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

    @Transactional(readOnly = true)
    public Cliente obtenerPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe un cliente con id " + id));
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return clienteRepository.findAll();
    }

    @Transactional
    public Cliente actualizar(Long id, Cliente datos) {
        Cliente existente = obtenerPorId(id);
        validarMayorDeEdad(datos.getFechaNacimiento());
        if (cambiaIdentificacion(existente, datos)) {
            validarIdentificacionNoRegistrada(datos.getTipoIdentificacion(),
                    datos.getNumeroIdentificacion());
        }

        existente.setTipoIdentificacion(datos.getTipoIdentificacion());

        existente.setNumeroIdentificacion(datos.getNumeroIdentificacion());
        existente.setNombres(datos.getNombres());
        existente.setApellido(datos.getApellido());
        existente.setCorreoElectronico(datos.getCorreoElectronico());
        existente.setFechaNacimiento(datos.getFechaNacimiento());
        return existente;
    }

    private void validarMayorDeEdad(LocalDate fechaNacimiento) {
        if (fechaNacimiento != null && !MayorDeEdadValidator.esMayorDeEdad(fechaNacimiento,
                MayorDeEdad.EDAD_MINIMA)) {
            throw new ReglaNegocioException("El cliente debe ser mayor de edad");
        }
    }

    private void validarIdentificacionNoRegistrada(TipoIdentificacion tipo, String numero) {
        if (clienteRepository.existsByTipoIdentificacionAndNumeroIdentificacion(tipo, numero)) {
            throw new ReglaNegocioException("Ya existe un cliente con " + tipo + " " + numero);
        }

    }

    private boolean cambiaIdentificacion(Cliente existente, Cliente datos) {
        return existente.getTipoIdentificacion() != datos.getTipoIdentificacion() ||
                !existente.getNumeroIdentificacion().equals(datos.getNumeroIdentificacion());
    }
}
