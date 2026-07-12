package com.goodNews.genesis.modulos.grupos.dtos;

import java.util.UUID;

import com.goodNews.genesis.shared.enums.GenerosEnum;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record GrupoRequestDTO(

        @NotBlank(message = "El nombre del grupo es obligatorio")
        String nombre,

        @NotNull(message = "El género es obligatorio")
        GenerosEnum genero,

        @NotNull(message = "La edad mínima es obligatoria")
        @Min(value = 0, message = "La edad mínima no puede ser negativa")
        Integer edadMinima,

        @NotNull(message = "La edad máxima es obligatoria")
        @Min(value = 1, message = "La edad máxima debe ser al menos 1")
        Integer edadMaxima,

        // Si no se envía, se usará el default de 5
        Integer capacidadMax,

        @NotNull(message = "Debe asignar un maestro al grupo")
        UUID maestroId

) {}
