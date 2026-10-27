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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @PostMapping
    public ResponseEntity<CuentaResponse> crear(@Valid @RequestBody CuentaRequest request) {
        Cuenta creada = cuentaService.crear(request.clienteId(), request.tipoCuenta(), request.exentaGmf());
        URI ubicacion = URI.create("/api/cuentas/" + creada.getId());
        return ResponseEntity.created(ubicacion).body(CuentaResponse.desde(creada));
    }

    @GetMapping("/{id}")
    public CuentaResponse obtenerPorId(@PathVariable Long id) {
        return CuentaResponse.desde(cuentaService.obtenerPorId(id));
    }

    @GetMapping
    public List<CuentaResponse> listarPorCliente(@RequestParam Long clienteId) {
        return cuentaService.listarPorCliente(clienteId).stream()
                .map(CuentaResponse::desde)
                .toList();
    }

    @PatchMapping("/{id}/activar")
    public CuentaResponse activar(@PathVariable Long id) {
        return CuentaResponse.desde(cuentaService.activar(id));
    }

    @PatchMapping("/{id}/inactivar")
    public CuentaResponse inactivar(@PathVariable Long id) {
        return CuentaResponse.desde(cuentaService.inactivar(id));
    }

    @PatchMapping("/{id}/cancelar")
    public CuentaResponse cancelar(@PathVariable Long id) {
        return CuentaResponse.desde(cuentaService.cancelar(id));
    }
}