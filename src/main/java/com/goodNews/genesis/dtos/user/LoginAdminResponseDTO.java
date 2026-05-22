package com.goodNews.genesis.dtos.user;

public record LoginAdminResponseDTO(
        String token,
        UserResponse user

) {
}
