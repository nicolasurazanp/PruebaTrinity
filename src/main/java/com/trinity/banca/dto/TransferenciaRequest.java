package com.trinity.banca.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransferenciaRequest(
        @NotNull Long cuentaOrigenId,
        @NotNull Long cuentaDestinoId,
        @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal monto) {
}
