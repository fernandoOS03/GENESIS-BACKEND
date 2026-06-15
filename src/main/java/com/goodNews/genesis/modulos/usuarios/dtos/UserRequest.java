package com.goodNews.genesis.modulos.usuarios.dtos;

import com.goodNews.genesis.shared.enums.UsersEnum;

public record UserRequest(
    String nombre,
    String email,
    String password,
    UsersEnum rol,
    String pais,
    Integer estado
) {

}
