package com.goodNews.genesis.controllers;

import com.goodNews.genesis.dtos.user.LoginAdminRequestDTO;
import com.goodNews.genesis.dtos.user.LoginAdminResponseDTO;
import com.goodNews.genesis.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/usuarios")
@RequiredArgsConstructor

public class UserController {
    private final AuthService authService;

    //Enpoint para el logeo del admin
    @PostMapping("/login")
    public ResponseEntity<LoginAdminResponseDTO> login(@RequestBody @Valid LoginAdminRequestDTO request){
        return ResponseEntity.ok(authService.login(request));
    };



}
