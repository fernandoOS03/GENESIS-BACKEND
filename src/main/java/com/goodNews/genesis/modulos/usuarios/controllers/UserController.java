package com.goodNews.genesis.modulos.usuarios.controllers;

import com.goodNews.genesis.modulos.usuarios.dtos.UserRequest;
import com.goodNews.genesis.modulos.auth.dtos.UserResponse;
import com.goodNews.genesis.modulos.usuarios.dtos.UserListResponse;
import com.goodNews.genesis.modulos.usuarios.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("api/usuarios")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Endpoint para registrar un nuevo usuario
    @PostMapping("/registrar")
    public ResponseEntity<UserResponse> crearUsuario(@RequestBody @Valid UserRequest request) {
        UserResponse response = userService.crearUsuario(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Endpoint para listar todos los usuarios
    @GetMapping
    public ResponseEntity<List<UserListResponse>> listarUsuarios() {
        List<UserListResponse> response = userService.listarUsuarios();
        return ResponseEntity.ok(response);
    }

    // Endpoint para cambiar el estado del usuario (soft delete)
    @PutMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable java.util.UUID id, @RequestParam Integer estado) {
        userService.cambiarEstado(id, estado);
        return ResponseEntity.ok().build();
    }
}
