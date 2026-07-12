package com.goodNews.genesis.modulos.pagos.controllers;

import com.goodNews.genesis.modulos.pagos.dtos.AbonoRequestDTO;
import com.goodNews.genesis.modulos.pagos.dtos.AbonoResponseDTO;
import com.goodNews.genesis.modulos.pagos.services.PayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class AccountPayController {

    private final PayService payService;

    @PostMapping
    public ResponseEntity<AbonoResponseDTO> agregarAbono(@RequestBody @Valid AbonoRequestDTO dto) {
        AbonoResponseDTO abono = payService.registrarNuevoAbono(dto);
        return  ResponseEntity.status(HttpStatus.OK).body(abono);
    }
}
