package com.goodNews.genesis.modulos.pagos.dtos;

import com.goodNews.genesis.shared.enums.MonedasEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record TarifaRequestDTO(
        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor a cero")
        Double monto,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate fechaFin,

        @NotNull(message = "La moneda es obligatoria")
        MonedasEnum moneda
) {}
