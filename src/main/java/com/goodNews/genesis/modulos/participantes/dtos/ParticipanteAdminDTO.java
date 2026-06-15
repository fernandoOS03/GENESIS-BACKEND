package com.goodNews.genesis.modulos.participantes.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

import com.goodNews.genesis.modulos.participantes.entities.ParticipantsEntity;

public record ParticipanteAdminDTO(
                UUID id,
                String nombres,
                String apellidos,
                String email,
                String pais,
                String telefono,
                String tipoDocumento,
                String nroDocumento,
                String genero,
                String sede,
                String tallaPolo,
                String condParticipacion,
                String rol,
                String estadoRegistro,
                LocalDateTime fechaRegistro,
                String tipoTransporte,
                String empresaTransporte,
                String nroVuelo,
                String lugarLlegada) {

        public ParticipanteAdminDTO(ParticipantsEntity entity) {
                this(
                        entity.getId(),
                        entity.getNombres(),
                        entity.getApellidos(),
                        entity.getEmail(),
                        entity.getPais(),
                        entity.getTelefono(),
                        entity.getTipoDocumento(),
                        entity.getNroDocumento(),
                        entity.getGenero() != null ? entity.getGenero().name() : null,
                        entity.getSede() != null ? entity.getSede().name() : null,
                        entity.getTallaPolo() != null ? entity.getTallaPolo().name() : null,
                        entity.getCondParticipacion() != null ? entity.getCondParticipacion().name() : null,
                        entity.getRol() != null ? entity.getRol().name() : null,
                        entity.getEstadoRegistro() != null ? entity.getEstadoRegistro().name() : null,
                        entity.getFechaRegistro(),
                        entity.getTravelInformation() != null && entity.getTravelInformation().getTipoTransporte() != null ? entity.getTravelInformation().getTipoTransporte().name(): null,
                        entity.getTravelInformation() != null ? entity.getTravelInformation().getEmpresaTransporte() : null,
                        entity.getTravelInformation() != null ? entity.getTravelInformation().getNroVuelo(): null,
                        entity.getTravelInformation() != null ? entity.getTravelInformation().getLugarLlegada(): null);
        }
}

