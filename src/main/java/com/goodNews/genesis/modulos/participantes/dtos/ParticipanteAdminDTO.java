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
                String estadoPago,
                String estadoTransporte,
                String estadoGeneral,
                LocalDateTime fechaRegistro,
                java.time.LocalDate fechaNacimiento,
                String tipoTransporte,
                String empresaTransporte,
                String nroVuelo,
                String lugarLlegada,
                LocalDateTime fechaLlegada,
                LocalDateTime fechaIda,
                UUID cuentaId,
                Double totalAbonado,
                Double tarifaCongelada,
                String cuentaMoneda,
                UUID grupoId,
                String grupoNombre) {

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
                        entity.getEstadoPago() != null ? entity.getEstadoPago().name() : null,
                        entity.getEstadoTransporte() != null ? entity.getEstadoTransporte().name() : null,
                        entity.getEstadoGeneral() != null ? entity.getEstadoGeneral().name() : null,
                        entity.getFechaRegistro(),
                        entity.getFechaNacimiento(),
                        entity.getTravelInformation() != null && entity.getTravelInformation().getTipoTransporte() != null ? entity.getTravelInformation().getTipoTransporte().name(): null,
                        entity.getTravelInformation() != null ? entity.getTravelInformation().getEmpresaTransporte() : null,
                        entity.getTravelInformation() != null ? entity.getTravelInformation().getNroVuelo(): null,
                        entity.getTravelInformation() != null ? entity.getTravelInformation().getLugarLlegada(): null,
                        entity.getTravelInformation() != null ? entity.getTravelInformation().getFechaLlegada(): null,
                        entity.getTravelInformation() != null ? entity.getTravelInformation().getFechaIda(): null,
                        entity.getAccountPay() != null ? entity.getAccountPay().getId() : null,
                        (entity.getAccountPay() != null && entity.getAccountPay().getTotalAbonado() != null) ? entity.getAccountPay().getTotalAbonado() : 0.0,
                        (entity.getAccountPay() != null && entity.getAccountPay().getTarifaCongelada() != null) ? entity.getAccountPay().getTarifaCongelada() : 0.0,
                        (entity.getAccountPay() != null && entity.getAccountPay().getMonedaCongelada() != null) ? entity.getAccountPay().getMonedaCongelada().name() : null,
                        entity.getGrupo() != null ? entity.getGrupo().getId() : null,
                        entity.getGrupo() != null ? entity.getGrupo().getNombre() : null);
        }
}
