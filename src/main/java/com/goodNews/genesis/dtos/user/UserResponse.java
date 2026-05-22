package com.goodNews.genesis.dtos.user;

import com.goodNews.genesis.enums.UsersEnum;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String Email,
        UsersEnum rol
) {
}
