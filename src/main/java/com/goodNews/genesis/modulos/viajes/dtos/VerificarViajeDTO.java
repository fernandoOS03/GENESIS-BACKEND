package com.goodNews.genesis.modulos.viajes.dtos;

import jakarta.validation.constraints.NotBlank;

public record VerificarViajeDTO(
        @NotBlank(message = "El número de documento es obligatorio")
        String nroDocumento,

        @NotBlank(message = "El código de viaje es obligatorio")
        String codigoViaje
) {
}
