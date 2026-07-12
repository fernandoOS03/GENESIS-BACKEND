package com.goodNews.genesis.modulos.grupos.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.goodNews.genesis.modulos.grupos.entities.GrupoEntity;
import com.goodNews.genesis.shared.enums.GenerosEnum;

@Repository
public interface GrupoRepository extends JpaRepository<GrupoEntity, UUID> {

    // Verificar si un participante ya es maestro de algún grupo
    Optional<GrupoEntity> findByMaestroId(UUID maestroId);

    // Buscar grupos por género (útil para asignación automática)
    List<GrupoEntity> findByGenero(GenerosEnum genero);

    // Grupos con capacidad disponible: cuenta alumnos (excluye maestro) < capacidadMax
    // Se usa en la asignación automática para filtrar grupos llenos
    @Query("""
            SELECT g FROM GrupoEntity g
            WHERE g.genero = :genero
            AND g.edadMinima <= :edad
            AND g.edadMaxima >= :edad
            AND (
                SELECT COUNT(p) FROM ParticipantsEntity p
                WHERE p.grupo = g
                AND (g.maestro IS NULL OR p.id <> g.maestro.id)
            ) < g.capacidadMax
            ORDER BY (
                SELECT COUNT(p) FROM ParticipantsEntity p
                WHERE p.grupo = g
                AND (g.maestro IS NULL OR p.id <> g.maestro.id)
            ) DESC
            """)
    List<GrupoEntity> findGruposDisponibles(@Param("genero") GenerosEnum genero, @Param("edad") int edad);
}
