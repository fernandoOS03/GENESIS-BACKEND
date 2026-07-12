package com.goodNews.genesis.modulos.viajes.dtos;

import java.util.UUID;

public record VerificacionResponseDTO(
        UUID participanteId,
        String nombres
) {
}
