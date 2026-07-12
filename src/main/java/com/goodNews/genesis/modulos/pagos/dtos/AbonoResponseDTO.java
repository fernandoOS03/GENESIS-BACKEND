package com.goodNews.genesis.modulos.pagos.dtos;

public record AbonoResponseDTO(
        String estado,
        Double montoActual,
        Double tarifaTotal,
        Double porcentaje
) {
}
