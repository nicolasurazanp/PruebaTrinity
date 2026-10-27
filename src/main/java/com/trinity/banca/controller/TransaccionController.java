package com.trinity.banca.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.trinity.banca.dto.EstadoCuentaResponse;
import com.trinity.banca.dto.MovimientoResponse;
import com.trinity.banca.dto.OperacionRequest;
import com.trinity.banca.dto.TransferenciaRequest;
import com.trinity.banca.entity.Cuenta;
import com.trinity.banca.entity.Movimiento;
import com.trinity.banca.service.CuentaService;
import com.trinity.banca.service.TransaccionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Transacciones", description = "Consignaciones, retiros, transferencias y estado de cuenta")
@RestController
@RequestMapping("/api")
public class TransaccionController {

    private final TransaccionService transaccionService;
    private final CuentaService cuentaService;

    public TransaccionController(TransaccionService transaccionService, CuentaService cuentaService) {
        this.transaccionService = transaccionService;
        this.cuentaService = cuentaService;
    }

    @Operation(summary = "Consignar en una cuenta")
    @PostMapping("/transacciones/consignaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoResponse consignar(@Valid @RequestBody OperacionRequest request) {
        return MovimientoResponse.desde(transaccionService.consignar(request.cuentaId(), request.monto()));
    }

    @Operation(summary = "Retirar de una cuenta")
    @PostMapping("/transacciones/retiros")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoResponse retirar(@Valid @RequestBody OperacionRequest request) {
        return MovimientoResponse.desde(transaccionService.retirar(request.cuentaId(), request.monto()));
    }

    @Operation(summary = "Transferir entre dos cuentas")
    @PostMapping("/transacciones/transferencias")
    @ResponseStatus(HttpStatus.CREATED)
    public List<MovimientoResponse> transferir(@Valid @RequestBody TransferenciaRequest request) {
        return transaccionService.transferir(request.cuentaOrigenId(), request.cuentaDestinoId(), request.monto())
                .stream()
                .map(MovimientoResponse::desde)
                .toList();
    }

    @Operation(summary = "Consultar el estado de cuenta")
    @GetMapping("/cuentas/{cuentaId}/estado-cuenta")
    public EstadoCuentaResponse estadoDeCuenta(@PathVariable Long cuentaId) {
        List<Movimiento> movimientos = transaccionService.estadoDeCuenta(cuentaId);
        Cuenta cuenta = cuentaService.obtenerPorId(cuentaId);
        return EstadoCuentaResponse.desde(cuenta, movimientos);
    }
}
