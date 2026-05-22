package com.goodNews.genesis.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goodNews.genesis.dtos.travel.ActualizarViajeDTO;
import com.goodNews.genesis.dtos.participant.ParticipanteAdminDTO;
import com.goodNews.genesis.dtos.participant.ParticipanteRegistroDTO;
import com.goodNews.genesis.dtos.participant.ParticipanteResponseDTO;
import com.goodNews.genesis.services.ParticipantService;
import com.goodNews.genesis.shared.services.JwtService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/participantes")
@RequiredArgsConstructor
public class ParticipantController {

	private final ParticipantService service;

	@Autowired
	private final JwtService jwtService;

	// ==========================================================
	// 1. ENDPOINT : Listar todos los participantes (Admin)
	// ==========================================================
	@GetMapping
	public ResponseEntity<List<ParticipanteAdminDTO>> listarTodos() {
		List<ParticipanteAdminDTO> participantes = service.listarTodos();
		return ResponseEntity.ok(participantes);
	}

	// ==========================================================
	// 2. ENDPOINT : Para que el usuario se pueda registrar
	// ==========================================================
	@PostMapping
	public ResponseEntity<ParticipanteResponseDTO> registrar(@RequestBody @Valid ParticipanteRegistroDTO dto) {

		ParticipanteResponseDTO respuesta = service.registrarParticipante(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
	}

	// ==========================================================
	// 3. ENDPOINT : Para que el usuario pueda actualizar su viaje
	// ==========================================================

	@PatchMapping("/viaje")
	public ResponseEntity<?> updateTravel(
			@RequestHeader("Authorization") String authHeader,
			@RequestBody @Valid ActualizarViajeDTO dto) {

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token de autorización ausente o incorrecto.");
		}

		String token = authHeader.substring(7);

		if (!jwtService.isTokenValid(token)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body("El enlace de edición ha expirado o es inválido.");
		}

		String idString = jwtService.extractUsername(token);
		UUID id = UUID.fromString(idString);

		ActualizarViajeDTO responseDTO = service.updateParticipantTravel(dto, id);
		return ResponseEntity.status(HttpStatus.OK).body(responseDTO);

	};

}
