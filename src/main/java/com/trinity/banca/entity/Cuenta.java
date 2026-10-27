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
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;

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

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false, updatable =
        false,
        foreignKey = @ForeignKey(name = "fk_cuentas_cliente"))
        private Cliente cliente;

        @Column(name = "fecha_creacion", nullable = false, updatable =
            false)
            private LocalDateTime fechaCreacion;

        @Column(name = "fecha_modificacion")
        private LocalDateTime fechaModificacion;
    

    protected Cuenta() {
        // Constructor protegido para JPA
    }

    @PrePersist
    protected void alCrear(){
        this.fechaCreacion = LocalDateTime.now();
    }
    @PreUpdate
    protected void alModificar(){
        this.fechaModificacion = LocalDateTime.now();
    }
}
