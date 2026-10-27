package com.trinity.banca.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(
    name = "cuentas",
    uniqueConstraints = @UniqueConstraint(name = "uk_cuentas_numero",
        columnNames = "numero_cuenta")
    )

public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta", nullable = false, length = 20)
    private TipoCuenta tipoCuenta;

    @NotNull
    @Pattern(regexp = "[0-9]{10}")
    @Column(name  = "numero_cuenta", nullable = false, length = 10,
        updatable = false)
    private String numerooCuenta;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column (name = "estado", nullable = false, length = 20)
    private EstadoCuenta estado;

    @NotNull 
    @Column(name = "saldo", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    @Column(name = "exenta_gmf", nullable = false)
    private boolean exentaGmf;

    protected Cuenta() {
        // Constructor protegido para JPA
    }
}
