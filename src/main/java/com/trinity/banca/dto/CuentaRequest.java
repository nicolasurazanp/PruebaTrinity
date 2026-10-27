package com.trinity.banca.dto;

import com.trinity.banca.entity.TipoCuenta;

import jakarta.validation.constraints.NotNull;

public record CuentaRequest(
        @NotNull Long clienteId,
        @NotNull TipoCuenta tipoCuenta,
        boolean exentaGmf) {
}