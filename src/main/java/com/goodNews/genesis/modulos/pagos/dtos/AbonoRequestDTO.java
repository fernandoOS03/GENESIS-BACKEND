package com.goodNews.genesis.modulos.pagos.dtos;

import java.util.UUID;

public record AbonoRequestDTO(
        UUID cuentaId,
        Double montoIngresado,
        String tipoMoneda
) {
}
