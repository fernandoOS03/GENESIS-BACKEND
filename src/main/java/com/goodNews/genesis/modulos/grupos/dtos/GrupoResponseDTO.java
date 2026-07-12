package com.goodNews.genesis.modulos.grupos.dtos;

import java.util.List;
import java.util.UUID;

import com.goodNews.genesis.modulos.grupos.entities.GrupoEntity;
import com.goodNews.genesis.modulos.participantes.entities.ParticipantsEntity;

/**
 * DTO de respuesta de un grupo.
 * Usado tanto para lista (sin {@code miembros}) como para detalle (con {@code miembros}).
 */
public record GrupoResponseDTO(
        UUID id,
        String nombre,
        String genero,
        Integer edadMinima,
        Integer edadMaxima,
        Integer capacidadMax,
        Integer alumnosActuales,
        UUID maestroId,
        String maestroNombre,
        List<MiembroDTO> miembros
) {

    // DTO interno para cada alumno del grupo
    public record MiembroDTO(UUID id, String nombres, String apellidos, String nroDocumento) {
        public MiembroDTO(ParticipantsEntity p) {
            this(p.getId(), p.getNombres(), p.getApellidos(), p.getNroDocumento());
        }
    }

    // Constructor de resumen — para el listado (sin lista de miembros)
    public GrupoResponseDTO(GrupoEntity grupo) {
        this(
                grupo.getId(),
                grupo.getNombre(),
                generoName(grupo),
                grupo.getEdadMinima(),
                grupo.getEdadMaxima(),
                grupo.getCapacidadMax(),
                calcularAlumnos(grupo),
                maestroId(grupo),
                maestroNombre(grupo),
                null
        );
    }

    // Constructor de detalle — con lista completa de alumnos
    public GrupoResponseDTO(GrupoEntity grupo, List<ParticipantsEntity> alumnos) {
        this(
                grupo.getId(),
                grupo.getNombre(),
                generoName(grupo),
                grupo.getEdadMinima(),
                grupo.getEdadMaxima(),
                grupo.getCapacidadMax(),
                alumnos.size(),
                maestroId(grupo),
                maestroNombre(grupo),
                alumnos.stream().map(MiembroDTO::new).toList()
        );
    }

    // =========================================================
    // Helpers estáticos — centralizan la lógica null-safe
    // En Java no existe el operador ?. de Kotlin, así que lo
    // encapsulamos en métodos para no repetir el ternario en
    // cada constructor.
    // =========================================================

    private static String generoName(GrupoEntity g) {
        return g.getGenero() != null ? g.getGenero().name() : null;
    }

    private static UUID maestroId(GrupoEntity g) {
        return g.getMaestro() != null ? g.getMaestro().getId() : null;
    }

    private static String maestroNombre(GrupoEntity g) {
        if (g.getMaestro() == null) return null;
        return g.getMaestro().getNombres() + " " + g.getMaestro().getApellidos();
    }

    private static int calcularAlumnos(GrupoEntity g) {
        if (g.getParticipantes() == null) return 0;
        UUID mid = maestroId(g);
        return (int) g.getParticipantes().stream()
                .filter(p -> mid == null || !p.getId().equals(mid))
                .count();
    }
}
