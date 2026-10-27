package com.trinity.banca.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trinity.banca.dto.CuentaRequest;
import com.trinity.banca.dto.CuentaResponse;
import com.trinity.banca.entity.Cuenta;
import com.trinity.banca.service.CuentaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Cuentas", description = "Apertura de cuentas de ahorros y corrientes, y cambios de estado")
@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @Operation(summary = "Abrir una cuenta")
    @PostMapping
    public ResponseEntity<CuentaResponse> crear(@Valid @RequestBody CuentaRequest request) {
        Cuenta creada = cuentaService.crear(request.clienteId(), request.tipoCuenta(), request.exentaGmf());
        URI ubicacion = URI.create("/api/cuentas/" + creada.getId());
        return ResponseEntity.created(ubicacion).body(CuentaResponse.desde(creada));
    }

    @Operation(summary = "Consultar una cuenta")
    @GetMapping("/{id}")
    public CuentaResponse obtenerPorId(@PathVariable Long id) {
        return CuentaResponse.desde(cuentaService.obtenerPorId(id));
    }

    @Operation(summary = "Listar las cuentas de un cliente")
    @GetMapping
    public List<CuentaResponse> listarPorCliente(@RequestParam Long clienteId) {
        return cuentaService.listarPorCliente(clienteId).stream()
                .map(CuentaResponse::desde)
                .toList();
    }

    @Operation(summary = "Activar una cuenta")
    @PatchMapping("/{id}/activar")
    public CuentaResponse activar(@PathVariable Long id) {
        return CuentaResponse.desde(cuentaService.activar(id));
    }

    @Operation(summary = "Inactivar una cuenta")
    @PatchMapping("/{id}/inactivar")
    public CuentaResponse inactivar(@PathVariable Long id) {
        return CuentaResponse.desde(cuentaService.inactivar(id));
    }

    @Operation(summary = "Cancelar una cuenta con saldo igual a $0")
    @PatchMapping("/{id}/cancelar")
    public CuentaResponse cancelar(@PathVariable Long id) {
        return CuentaResponse.desde(cuentaService.cancelar(id));
    }
}