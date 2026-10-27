package com.trinity.banca.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.trinity.banca.entity.Movimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
}
