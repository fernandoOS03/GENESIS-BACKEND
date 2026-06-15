package com.goodNews.genesis.modulos.auth.controllers;

import com.goodNews.genesis.modulos.auth.dtos.LoginAdminRequestDTO;
import com.goodNews.genesis.modulos.auth.dtos.LoginAdminResponseDTO;
import com.goodNews.genesis.modulos.auth.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class    AuthController {
    private final AuthService authService;

    @PostMapping()
    public ResponseEntity<LoginAdminResponseDTO> login(@RequestBody @Valid LoginAdminRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
