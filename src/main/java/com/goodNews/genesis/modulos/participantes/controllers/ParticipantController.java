package com.goodNews.genesis.modulos.participantes.controllers;

import java.util.List;
import java.util.UUID;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goodNews.genesis.modulos.viajes.dtos.ActualizarViajeDTO;
import com.goodNews.genesis.modulos.viajes.dtos.VerificarViajeDTO;
import com.goodNews.genesis.modulos.viajes.dtos.VerificacionResponseDTO;
import com.goodNews.genesis.modulos.participantes.dtos.ParticipanteAdminDTO;
import com.goodNews.genesis.modulos.participantes.dtos.ParticipanteRegistroDTO;
import com.goodNews.genesis.modulos.participantes.dtos.ParticipanteResponseDTO;
import com.goodNews.genesis.modulos.participantes.services.ParticipantService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/participantes")
@RequiredArgsConstructor
public class ParticipantController {

	private final ParticipantService service;

	// ==========================================================
	// 1. ENDPOINT : Listar todos los participantes (Admin)
	// ==========================================================
	@GetMapping
	public ResponseEntity<List<ParticipanteAdminDTO>> listarTodos() {
		List<ParticipanteAdminDTO> participantes = service.listarTodos();
		return ResponseEntity.ok(participantes);
	}

	// ==========================================================
	// 2. ENDPOINT PÚBLICO: El participante se registra a sí mismo
	// ==========================================================
	@PostMapping
	public ResponseEntity<ParticipanteResponseDTO> registrar(@RequestBody @Valid ParticipanteRegistroDTO dto) {
		ParticipanteResponseDTO respuesta = service.registrarParticipante(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
	}

	// ==========================================================
	// 3. ENDPOINT PROTEGIDO: Un admin/editor registra participante desde su panel
	// (requiere Bearer token de admin autenticado)
	// ==========================================================
	@PostMapping("/admin")
	public ResponseEntity<ParticipanteResponseDTO> registrarDesdeAdmin(@RequestBody @Valid ParticipanteRegistroDTO dto) {
		ParticipanteResponseDTO respuesta = service.registrarParticipanteDesdeAdmin(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
	}

	// ==========================================================
	// 4. ENDPOINT PÚBLICO: Verificar nroDocumento + código de viaje
	// ==========================================================
	@PostMapping("/viaje/verificar")
	public ResponseEntity<VerificacionResponseDTO> verificarCodigo(
			@RequestBody @Valid VerificarViajeDTO dto) {
		return ResponseEntity.ok(service.verificarCodigoViaje(dto));
	}

	// ==========================================================
	// 5. ENDPOINT PÚBLICO: El participante actualiza su viaje (usa ID del paso anterior)
	// ==========================================================
	@PatchMapping("/viaje/{id}")
	public ResponseEntity<ActualizarViajeDTO> actualizarViaje(
			@PathVariable UUID id,
			@RequestBody @Valid ActualizarViajeDTO dto) {
		return ResponseEntity.ok(service.actualizarViajeParticipante(dto, id));
	}

}


