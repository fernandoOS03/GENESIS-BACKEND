package com.goodNews.genesis.modulos.auth.dtos;

import java.util.UUID;

import com.goodNews.genesis.shared.enums.UsersEnum;

public record UserResponse(
        UUID id,
        String name,
        String email,
        UsersEnum rol,
        Integer estado,
        String pais
) {
}


