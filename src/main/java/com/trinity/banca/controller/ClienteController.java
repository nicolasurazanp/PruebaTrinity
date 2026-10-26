package com.trinity.banca.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trinity.banca.dto.ClienteRequest;
import com.trinity.banca.dto.ClienteResponse;
import com.trinity.banca.entity.Cliente;
import com.trinity.banca.service.ClienteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")

public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService){
        this.clienteService = clienteService;
    }

    @PostMapping 
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody
        ClienteRequest request){
            Cliente creado = clienteService.crear(request.aEntidad());
            URI ubicacion = URI.create("/api/clientes/" + creado.getId());
            return ResponseEntity.created(ubicacion).body(ClienteResponse.desde(creado));
        }
    @GetMapping 
    public List<ClienteResponse> listar(){
        return clienteService.listar().stream()
        .map(ClienteResponse::desde)
        .toList();
    }
    @GetMapping ("/{id}")
    public ClienteResponse obtenerPorId(@PathVariable Long id){
        return ClienteResponse.desde(clienteService.obtenerPorId(id));
    }
    @PutMapping("/{id}")
    public ClienteResponse actualizar(@PathVariable Long id, @Valid 
        @RequestBody ClienteRequest request){
        return ClienteResponse.desde(clienteService.actualizar(id,
            request.aEntidad()));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id){
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
