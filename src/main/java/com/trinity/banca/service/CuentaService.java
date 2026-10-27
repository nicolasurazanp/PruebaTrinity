package com.trinity.banca.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trinity.banca.entity.Cliente;
import com.trinity.banca.entity.Cuenta;
import com.trinity.banca.entity.TipoCuenta;
import com.trinity.banca.exception.RecursoNoEncontradoException;
import com.trinity.banca.repository.CuentaRepository;

@Service 
public class CuentaService {
    private final CuentaRepository cuentaRepository;
    private final ClienteService clienteService;
    private final GeneradorNumeroCuenta generadorNumeroCuenta;

    public CuentaService(CuentaRepository cuentaRepository, ClienteService clienteService,
        GeneradorNumeroCuenta generadorNumeroCuenta) {
            this.cuentaRepository = cuentaRepository;
            this.clienteService = clienteService;
            this.generadorNumeroCuenta = generadorNumeroCuenta;
        }
    @Transactional 
    public Cuenta crear(Long ClienteId, TipoCuenta tipoCuenta, boolean exentaGmf) {
        Cliente cliente = clienteService.obtenerPorId(ClienteId);
        String numeroCuenta =
        generadorNumeroCuenta.generar(tipoCuenta);
        return cuentaRepository.save(new Cuenta(tipoCuenta, numeroCuenta, exentaGmf, cliente));
    }

    @Transactional(readOnly = true)
    public Cuenta obtenerPorId(Long id){
        return cuentaRepository.findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("No existe una cuenta con id " + id));
    }

    @Transactional(readOnly = true)
    public List<Cuenta> listarPorCliente(Long clienteId){
        clienteService.obtenerPorId(clienteId);
        return cuentaRepository.findByClienteId(clienteId);
    }

    @Transactional
    public Cuenta activar(Long id){
        Cuenta cuenta = obtenerPorId(id);
        cuenta.activar();
        return cuenta;
    }

    @Transactional
    public Cuenta inactivar(Long id) {
        Cuenta cuenta = obtenerPorId(id);
        cuenta.inactivar();
        return cuenta;
    }

    @Transactional 
    public Cuenta cancelar(Long id){
        Cuenta cuenta = obtenerPorId(id);
        cuenta.cancelar();
        return cuenta;
    }
}
