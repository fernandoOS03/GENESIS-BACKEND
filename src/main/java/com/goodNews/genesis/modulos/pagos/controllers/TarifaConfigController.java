package com.goodNews.genesis.modulos.pagos.controllers;

import com.goodNews.genesis.modulos.pagos.dtos.TarifaRequestDTO;
import com.goodNews.genesis.modulos.pagos.dtos.TarifaResponseDTO;
import com.goodNews.genesis.modulos.pagos.services.TarifaConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tarifas")
@RequiredArgsConstructor
public class TarifaConfigController {

    private final TarifaConfigService service;

    @PostMapping
    public ResponseEntity<TarifaResponseDTO> crearTarifa(@RequestBody @Valid TarifaRequestDTO dto) {
        TarifaResponseDTO response = service.crearTarifa(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TarifaResponseDTO>> listarTodas() {
        List<TarifaResponseDTO> response = service.listarTodas();
        return ResponseEntity.ok(response);
    }
}
