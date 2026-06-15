package com.goodNews.genesis.modulos.participantes.dtos;

import com.goodNews.genesis.modulos.participantes.entities.ParticipantsEntity;

public record ParticipanteResponseDTO(
        String nombres,
        String apellidos,
        String nmroDocumento,
        String estadoRegistro) {
	public ParticipanteResponseDTO(ParticipantsEntity entity) {
		this(
				entity.getNombres(),          
	            entity.getApellidos(),        
	            entity.getNroDocumento(),     
	            entity.getEstadoRegistro() != null ? entity.getEstadoRegistro().name() : null
				);
				
	}
	
}


