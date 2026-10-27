package com.trinity.banca.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "movimientos", indexes = @Index(name = "idx_movimientos_cuenta_fecha", columnList = "cuenta_id, fecha"))
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "codigo_transaccion", nullable = false, length = 36, updatable = false)
    private String codigoTransaccion;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_movimientos_cuenta"))
    private Cuenta cuenta;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_transaccion", nullable = false, length = 20, updatable = false)
    private TipoTransaccion tipoTransaccion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 10, updatable = false)
    private TipoMovimiento tipoMovimiento;

    @NotNull
    @Positive
    @Column(name = "monto", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal monto;

    @NotNull
    @Column(name = "saldo_resultante", nullable = false, precision = 19, scale = 2, updatable = false)
    private BigDecimal saldoResultante;

    @Column(name = "fecha", nullable = false, updatable = false)
    private LocalDateTime fecha;

    protected Movimiento() {
        // Constructor requerido por JPA
    }

    public Movimiento(String codigoTransaccion, Cuenta cuenta, TipoTransaccion tipoTransaccion,
            TipoMovimiento tipoMovimiento, BigDecimal monto) {
        this.codigoTransaccion = codigoTransaccion;
        this.cuenta = cuenta;
        this.tipoTransaccion = tipoTransaccion;
        this.tipoMovimiento = tipoMovimiento;
        this.monto = monto;
        this.saldoResultante = cuenta.getSaldo();
    }

    @PrePersist
    protected void alCrear() {
        this.fecha = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getCodigoTransaccion() {
        return codigoTransaccion;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public TipoTransaccion getTipoTransaccion() {
        return tipoTransaccion;
    }

    public TipoMovimiento getTipoMovimiento() {
        return tipoMovimiento;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public BigDecimal getSaldoResultante() {
        return saldoResultante;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
