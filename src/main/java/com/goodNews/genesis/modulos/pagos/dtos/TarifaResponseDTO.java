package com.goodNews.genesis.modulos.pagos.dtos;

import com.goodNews.genesis.modulos.pagos.entities.TarifaConfigEntity;
import com.goodNews.genesis.shared.enums.MonedasEnum;
import java.time.LocalDate;
import java.util.UUID;

public record TarifaResponseDTO(
        UUID id,
        Double monto,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        MonedasEnum moneda
) {
    public TarifaResponseDTO(TarifaConfigEntity entity) {
        this(entity.getId(), entity.getMonto(), entity.getFechaInicio(), entity.getFechaFin(), entity.getMoneda());
    }
}
