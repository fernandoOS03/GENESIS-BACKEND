package com.goodNews.genesis.dtos.participant;

import com.goodNews.genesis.entities.ParticipantsEntity;

public record ParticipanteResponseDTO(
        String nombres,
        String apellidos,
        String nmroDocumento,
        String codigoReserva,
        String estadoRegistro) {
	public ParticipanteResponseDTO(ParticipantsEntity entity) {
		this(
				entity.getNombres(),          
	            entity.getApellidos(),        
	            entity.getNroDocumento(),     
	            entity.getCodigoReserva(),
	            entity.getEstadoRegistro()
				);
				
	}
	
}
