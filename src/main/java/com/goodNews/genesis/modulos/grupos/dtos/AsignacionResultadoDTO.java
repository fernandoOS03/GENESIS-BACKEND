package com.goodNews.genesis.modulos.grupos.dtos;

import java.util.List;
import java.util.UUID;

/**
 * Resultado de la operación de asignación masiva de grupos.
 */
public record AsignacionResultadoDTO(
        int totalAsignados,
        int totalSinGrupo,
        List<UUID> sinGrupoIds
) {}
