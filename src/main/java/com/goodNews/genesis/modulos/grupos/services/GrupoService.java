package com.goodNews.genesis.modulos.grupos.services;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.goodNews.genesis.core.exceptions.BadRequestException;
import com.goodNews.genesis.core.exceptions.ResourceNotFoundException;
import com.goodNews.genesis.modulos.grupos.dtos.AsignacionResultadoDTO;
import com.goodNews.genesis.modulos.grupos.dtos.GrupoRequestDTO;
import com.goodNews.genesis.modulos.grupos.dtos.GrupoResponseDTO;
import com.goodNews.genesis.modulos.grupos.entities.GrupoEntity;
import com.goodNews.genesis.modulos.grupos.repositories.GrupoRepository;
import com.goodNews.genesis.modulos.participantes.entities.ParticipantsEntity;
import com.goodNews.genesis.modulos.participantes.repositories.ParticipantRepository;
import com.goodNews.genesis.shared.enums.EstadoGeneralEnum;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final ParticipantRepository participantRepository;

    // =========================================================
    // CRUD
    // =========================================================

    /**
     * Crea un grupo vacío.
     * Reglas:
     * - edadMinima < edadMaxima
     * - El maestro debe existir y su género debe coincidir con el del grupo.
     * - Un participante solo puede ser maestro de un grupo a la vez.
     * - Al asignarse como maestro, su grupo_id pasa a ser este grupo (para que
     *   no aparezca como "sin grupo" en la asignación masiva).
     */
    public GrupoResponseDTO crearGrupo(GrupoRequestDTO dto) {
        validarRangoEdad(dto.edadMinima(), dto.edadMaxima());

        ParticipantsEntity maestro = obtenerMaestroValidado(dto.maestroId(), dto.genero().name());

        GrupoEntity grupo = new GrupoEntity();
        grupo.setNombre(dto.nombre());
        grupo.setGenero(dto.genero());
        grupo.setEdadMinima(dto.edadMinima());
        grupo.setEdadMaxima(dto.edadMaxima());
        grupo.setCapacidadMax(dto.capacidadMax() != null ? dto.capacidadMax() : 5);
        grupo.setMaestro(maestro);

        GrupoEntity guardado = grupoRepository.save(grupo);

        // Asignar al maestro a su propio grupo para que no aparezca como "sin grupo"
        maestro.setGrupo(guardado);
        participantRepository.save(maestro);

        log.info("Grupo '{}' creado con maestro '{}'", guardado.getNombre(), maestro.getNombres());
        return new GrupoResponseDTO(guardado);
    }

    /**
     * Lista todos los grupos con resumen (sin lista de miembros).
     */
    public List<GrupoResponseDTO> listarGrupos() {
        return grupoRepository.findAll()
                .stream()
                .map(GrupoResponseDTO::new)
                .toList();
    }

    /**
     * Detalle de un grupo con la lista completa de alumnos (excluye al maestro de la lista).
     */
    public GrupoResponseDTO obtenerGrupoPorId(UUID id) {
        GrupoEntity grupo = encontrarGrupo(id);

        // Alumnos = todos los participantes del grupo EXCEPTO el maestro
        List<ParticipantsEntity> alumnos = obtenerAlumnos(grupo);

        return new GrupoResponseDTO(grupo, alumnos);
    }

    /**
     * Edita los datos de un grupo.
     * Si se cambia el maestro:
     * - El maestro anterior pierde su grupo_id (queda sin grupo).
     * - El nuevo maestro gana el grupo_id de este grupo.
     */
    public GrupoResponseDTO editarGrupo(UUID id, GrupoRequestDTO dto) {
        validarRangoEdad(dto.edadMinima(), dto.edadMaxima());

        GrupoEntity grupo = encontrarGrupo(id);
        ParticipantsEntity maestroActual = grupo.getMaestro();

        // Verificar si cambió el maestro
        boolean cambioMaestro = maestroActual == null || !maestroActual.getId().equals(dto.maestroId());

        if (cambioMaestro) {
            ParticipantsEntity nuevoMaestro = obtenerMaestroValidado(dto.maestroId(), dto.genero().name());

            // Liberar al maestro anterior
            if (maestroActual != null) {
                maestroActual.setGrupo(null);
                participantRepository.save(maestroActual);
            }

            // Asignar al nuevo maestro
            nuevoMaestro.setGrupo(grupo);
            participantRepository.save(nuevoMaestro);
            grupo.setMaestro(nuevoMaestro);
        }

        grupo.setNombre(dto.nombre());
        grupo.setGenero(dto.genero());
        grupo.setEdadMinima(dto.edadMinima());
        grupo.setEdadMaxima(dto.edadMaxima());
        if (dto.capacidadMax() != null) {
            grupo.setCapacidadMax(dto.capacidadMax());
        }

        GrupoEntity actualizado = grupoRepository.save(grupo);
        return new GrupoResponseDTO(actualizado);
    }

    /**
     * Elimina un grupo.
     * Todos los participantes (incluido el maestro) quedan con grupo_id = null.
     */
    public void eliminarGrupo(UUID id) {
        GrupoEntity grupo = encontrarGrupo(id);

        // Desasignar a todos los participantes del grupo
        List<ParticipantsEntity> todos = participantRepository.findByGrupoId(id);
        todos.forEach(p -> p.setGrupo(null));
        participantRepository.saveAll(todos);

        grupoRepository.delete(grupo);
        log.info("Grupo '{}' eliminado. {} participantes desasignados.", grupo.getNombre(), todos.size());
    }

    // =========================================================
    // Gestión manual de miembros
    // =========================================================

    /**
     * Remueve un alumno de un grupo (grupo_id pasa a null).
     * No permite remover al maestro con este endpoint.
     */
    public void removerParticipanteDeGrupo(UUID grupoId, UUID participanteId) {
        GrupoEntity grupo = encontrarGrupo(grupoId);

        // Proteger al maestro
        if (grupo.getMaestro() != null && grupo.getMaestro().getId().equals(participanteId)) {
            throw new BadRequestException(
                    "No puedes remover al maestro del grupo. " +
                    "Para cambiar al maestro, edita el grupo.");
        }

        ParticipantsEntity participante = participantRepository.findById(participanteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Participante no encontrado: " + participanteId));

        if (participante.getGrupo() == null || !participante.getGrupo().getId().equals(grupoId)) {
            throw new BadRequestException(
                    "El participante no pertenece a este grupo.");
        }

        participante.setGrupo(null);
        participantRepository.save(participante);
        log.info("Participante '{}' removido del grupo '{}'", participanteId, grupo.getNombre());
    }

    /**
     * Reasigna un participante a un grupo diferente.
     * Valida que el grupo destino tenga capacidad y que el género coincida.
     * No permite reasignar maestros.
     */
    public GrupoResponseDTO reasignarParticipante(UUID participanteId, UUID nuevoGrupoId) {
        ParticipantsEntity participante = participantRepository.findById(participanteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Participante no encontrado: " + participanteId));

        // Solo participantes CONFIRMADOS pueden ser reasignados
        validarEstadoConfirmado(participante, "alumno");

        GrupoEntity nuevoGrupo = encontrarGrupo(nuevoGrupoId);

        // No permitir reasignar a un maestro de otro grupo
        grupoRepository.findByMaestroId(participanteId).ifPresent(g -> {
            if (!g.getId().equals(nuevoGrupoId)) {
                throw new BadRequestException(
                        "El participante es maestro del grupo '" + g.getNombre() +
                        "'. Para reasignarlo debes primero cambiar el maestro de ese grupo.");
            }
        });

        // Validar género
        if (!participante.getGenero().equals(nuevoGrupo.getGenero())) {
            throw new BadRequestException(
                    "El género del participante (" + participante.getGenero() +
                    ") no coincide con el del grupo (" + nuevoGrupo.getGenero() + ").");
        }

        // Validar capacidad
        List<ParticipantsEntity> alumnos = obtenerAlumnos(nuevoGrupo);
        if (alumnos.size() >= nuevoGrupo.getCapacidadMax()) {
            throw new BadRequestException(
                    "El grupo '" + nuevoGrupo.getNombre() + "' ya alcanzó su capacidad máxima (" +
                    nuevoGrupo.getCapacidadMax() + " alumnos).");
        }

        participante.setGrupo(nuevoGrupo);
        participantRepository.save(participante);

        List<ParticipantsEntity> alumnosActualizados = obtenerAlumnos(nuevoGrupo);
        return new GrupoResponseDTO(nuevoGrupo, alumnosActualizados);
    }

    // =========================================================
    // Asignación masiva automática
    // =========================================================

    /**
     * Asigna automáticamente todos los participantes sin grupo a su grupo correspondiente.
     *
     * <p>Algoritmo:
     * <ol>
     *   <li>Obtiene todos los participantes con {@code grupo_id = NULL}.
     *       Los maestros ya tienen su grupo_id seteado, por lo que no aparecen aquí.</li>
     *   <li>Calcula la edad real de cada participante a partir de {@code fechaNacimiento}.</li>
     *   <li>Busca grupos disponibles donde género + rango de edad coinciden y aún hay capacidad.</li>
     *   <li>Asigna al primero disponible (más lleno primero, para compactar grupos).</li>
     *   <li>Los que no encajan quedan sin grupo y se retornan en el resultado.</li>
     * </ol>
     */
    public AsignacionResultadoDTO asignarGruposMasivo() {
        List<ParticipantsEntity> sinGrupo = participantRepository.findByGrupoIsNull();
        log.info("Iniciando asignación masiva. Participantes sin grupo: {}", sinGrupo.size());

        List<ParticipantsEntity> asignados = new ArrayList<>();
        List<UUID> noAsignados = new ArrayList<>();

        for (ParticipantsEntity participante : sinGrupo) {
            // Solo se asignan participantes CONFIRMADOS
            if (participante.getEstadoGeneral() != EstadoGeneralEnum.CONFIRMADO) {
                noAsignados.add(participante.getId());
                log.debug("Saltando participante {} — estado: {}",
                        participante.getId(), participante.getEstadoGeneral());
                continue;
            }

            if (participante.getFechaNacimiento() == null || participante.getGenero() == null) {
                noAsignados.add(participante.getId());
                continue;
            }

            int edad = calcularEdad(participante.getFechaNacimiento());

            List<GrupoEntity> candidatos = grupoRepository.findGruposDisponibles(
                    participante.getGenero(), edad);

            if (candidatos.isEmpty()) {
                noAsignados.add(participante.getId());
                log.debug("Sin grupo para participante {} (género={}, edad={})",
                        participante.getId(), participante.getGenero(), edad);
            } else {
                // Tomar el primero (el más lleno con espacio = estrategia de compactación)
                participante.setGrupo(candidatos.get(0));
                asignados.add(participante);
            }
        }

        if (!asignados.isEmpty()) {
            participantRepository.saveAll(asignados);
        }

        log.info("Asignación masiva completada. Asignados: {}, Sin grupo: {}",
                asignados.size(), noAsignados.size());

        return new AsignacionResultadoDTO(asignados.size(), noAsignados.size(), noAsignados);
    }

    // =========================================================
    // Helpers privados
    // =========================================================

    private GrupoEntity encontrarGrupo(UUID id) {
        return grupoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado: " + id));
    }

    /**
     * Valida y retorna el maestro.
     * Reglas:
     * - Debe existir como participante.
     * - Su género debe coincidir con el grupo.
     * - No puede ser maestro de otro grupo diferente (puede serlo del mismo al editar).
     */
    private ParticipantsEntity obtenerMaestroValidado(UUID maestroId, String generoGrupo) {
        ParticipantsEntity maestro = participantRepository.findById(maestroId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El participante seleccionado como maestro no existe: " + maestroId));

        // Solo participantes CONFIRMADOS pueden ser maestros
        validarEstadoConfirmado(maestro, "maestro");

        if (maestro.getGenero() == null || !maestro.getGenero().name().equals(generoGrupo)) {
            throw new BadRequestException(
                    "El maestro debe ser del mismo género que el grupo. " +
                    "Género del grupo: " + generoGrupo +
                    ", Género del maestro: " + (maestro.getGenero() != null ? maestro.getGenero().name() : "no definido"));
        }

        // Verificar que no sea maestro de otro grupo
        grupoRepository.findByMaestroId(maestroId).ifPresent(g -> {
            throw new BadRequestException(
                    "El participante ya es maestro del grupo '" + g.getNombre() +
                    "'. Un participante solo puede liderar un grupo a la vez.");
        });

        return maestro;
    }

    /**
     * Lanza excepción si el participante no está en estado CONFIRMADO.
     * Aplica tanto para maestros como para alumnos.
     */
    private void validarEstadoConfirmado(ParticipantsEntity participante, String rol) {
        if (participante.getEstadoGeneral() != EstadoGeneralEnum.CONFIRMADO) {
            throw new BadRequestException(
                    "El participante '" + participante.getNombres() + " " + participante.getApellidos() +
                    "' no puede ser asignado como " + rol + " porque su estado general es '" +
                    participante.getEstadoGeneral() + "'. Solo se permiten participantes CONFIRMADOS.");
        }
    }

    private void validarRangoEdad(Integer edadMinima, Integer edadMaxima) {
        if (edadMinima >= edadMaxima) {
            throw new BadRequestException(
                    "La edad mínima (" + edadMinima + ") debe ser menor que la edad máxima (" + edadMaxima + ").");
        }
    }

    /**
     * Retorna los alumnos del grupo, excluyendo al maestro de la lista.
     */
    private List<ParticipantsEntity> obtenerAlumnos(GrupoEntity grupo) {
        List<ParticipantsEntity> todos = participantRepository.findByGrupoId(grupo.getId());
        UUID maestroId = grupo.getMaestro() != null ? grupo.getMaestro().getId() : null;
        return todos.stream()
                .filter(p -> maestroId == null || !p.getId().equals(maestroId))
                .toList();
    }

    /**
     * Calcula la edad en años completos a partir de la fecha de nacimiento.
     */
    private int calcularEdad(LocalDate fechaNacimiento) {
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }
}
