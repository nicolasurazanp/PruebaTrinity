package com.trinity.banca.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.trinity.banca.entity.Cliente;
import com.trinity.banca.entity.TipoIdentificacion;

public interface ClienteRepository extends JpaRepository<Cliente, Long>{
    boolean existsByTipoIdentificacionAndNumeroIdentificacion(
        TipoIdentificacion tipoIdentificacion,
        String numeroIdentificacion);
}
