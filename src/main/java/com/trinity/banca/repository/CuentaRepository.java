package com.trinity.banca.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.trinity.banca.entity.Cuenta;


public interface CuentaRepository  extends JpaRepository<Cuenta, Long> {
    boolean existsByNumeroCuenta(String numeroCuenta);
    boolean existsByClienteId(Long clienteId);
    List<Cuenta> findByClienteId(Long clienteId);
}
