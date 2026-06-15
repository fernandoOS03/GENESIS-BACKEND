package com.goodNews.genesis.modulos.auth.dtos;

public record LoginAdminResponseDTO(
        String token,
        UserResponse user
) {
}
