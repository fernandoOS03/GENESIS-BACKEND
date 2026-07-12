package com.goodNews.genesis.modulos.participantes.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goodNews.genesis.modulos.participantes.entities.ParticipantsEntity;
import com.goodNews.genesis.shared.enums.EstadoGeneralEnum;

@Repository
public interface ParticipantRepository extends JpaRepository<ParticipantsEntity, UUID> {
    // se tiene que actualizar porque si alguien se registra con otro documento, ya
    // sea pasaorte o cedula, esto pasara

    boolean existsByNroDocumento(String nroDocumento);

    Optional<ParticipantsEntity> findById(UUID id);

    Optional<ParticipantsEntity> findByNombres(String nombres);

    Optional<ParticipantsEntity> findByNroDocumentoAndCodigoViaje(String nroDocumento, String codigoViaje);

    List<ParticipantsEntity> findByPaisIgnoreCase(String pais);

    List<ParticipantsEntity> findByEstadoGeneral(EstadoGeneralEnum estadoGeneral);

    List<ParticipantsEntity> findByGrupoIsNull();

    List<ParticipantsEntity> findByGrupoId(UUID grupoId);
}




