package com.goodNews.genesis.modulos.participantes.dtos;

import com.goodNews.genesis.modulos.participantes.entities.ParticipantsEntity;

public record ParticipanteResponseDTO(
        String nombres,
        String apellidos,
        String nmroDocumento,
        String estadoPago,
        String estadoTransporte,
        String estadoGeneral) {
	public ParticipanteResponseDTO(ParticipantsEntity entity) {
		this(
				entity.getNombres(),          
	            entity.getApellidos(),        
	            entity.getNroDocumento(),     
	            entity.getEstadoPago() != null ? entity.getEstadoPago().name() : null,
	            entity.getEstadoTransporte() != null ? entity.getEstadoTransporte().name() : null,
	            entity.getEstadoGeneral() != null ? entity.getEstadoGeneral().name() : null
				);
				
	}
	
}


