package com.goodNews.genesis.modulos.grupos.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.goodNews.genesis.modulos.grupos.dtos.AsignacionResultadoDTO;
import com.goodNews.genesis.modulos.grupos.dtos.GrupoRequestDTO;
import com.goodNews.genesis.modulos.grupos.dtos.GrupoResponseDTO;
import com.goodNews.genesis.modulos.grupos.services.GrupoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/grupos")
@RequiredArgsConstructor
public class GrupoController {

    private final GrupoService grupoService;

    // ==========================================================
    // 1. ENDPOINT: Listar todos los grupos (resumen)
    // ==========================================================
    @GetMapping
    public ResponseEntity<List<GrupoResponseDTO>> listarGrupos() {
        return ResponseEntity.ok(grupoService.listarGrupos());
    }

    // ==========================================================
    // 2. ENDPOINT: Detalle de un grupo (con lista de alumnos)
    // ==========================================================
    @GetMapping("/{id}")
    public ResponseEntity<GrupoResponseDTO> obtenerGrupo(@PathVariable UUID id) {
        return ResponseEntity.ok(grupoService.obtenerGrupoPorId(id));
    }

    // ==========================================================
    // 3. ENDPOINT: Crear grupo vacío (solo SUPER_ADMIN)
    // ==========================================================
    @PostMapping
    public ResponseEntity<GrupoResponseDTO> crearGrupo(@RequestBody @Valid GrupoRequestDTO dto) {
        GrupoResponseDTO creado = grupoService.crearGrupo(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // ==========================================================
    // 4. ENDPOINT: Editar grupo
    // ==========================================================
    @PutMapping("/{id}")
    public ResponseEntity<GrupoResponseDTO> editarGrupo(
            @PathVariable UUID id,
            @RequestBody @Valid GrupoRequestDTO dto) {
        return ResponseEntity.ok(grupoService.editarGrupo(id, dto));
    }

    // ==========================================================
    // 5. ENDPOINT: Eliminar grupo
    // ==========================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarGrupo(@PathVariable UUID id) {
        grupoService.eliminarGrupo(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================================
    // 6. ENDPOINT: Remover un participante (alumno) de un grupo
    // ==========================================================
    @DeleteMapping("/{grupoId}/participantes/{participanteId}")
    public ResponseEntity<Void> removerParticipante(
            @PathVariable UUID grupoId,
            @PathVariable UUID participanteId) {
        grupoService.removerParticipanteDeGrupo(grupoId, participanteId);
        return ResponseEntity.noContent().build();
    }

    // ==========================================================
    // 7. ENDPOINT: Reasignar un participante a otro grupo
    //    PATCH /api/v1/grupos/participantes/{participanteId}/reasignar?nuevoGrupoId={uuid}
    // ==========================================================
    @PatchMapping("/participantes/{participanteId}/reasignar")
    public ResponseEntity<GrupoResponseDTO> reasignarParticipante(
            @PathVariable UUID participanteId,
            @RequestParam UUID nuevoGrupoId) {
        return ResponseEntity.ok(grupoService.reasignarParticipante(participanteId, nuevoGrupoId));
    }

    // ==========================================================
    // 8. ENDPOINT: Asignación masiva automática (solo SUPER_ADMIN)
    // ==========================================================
    @PostMapping("/asignar")
    public ResponseEntity<AsignacionResultadoDTO> asignarGruposMasivo() {
        AsignacionResultadoDTO resultado = grupoService.asignarGruposMasivo();
        return ResponseEntity.ok(resultado);
    }
}
