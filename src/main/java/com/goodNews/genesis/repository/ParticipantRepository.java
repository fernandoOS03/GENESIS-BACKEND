package com.goodNews.genesis.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goodNews.genesis.entities.ParticipantsEntity;

@Repository
public interface ParticipantRepository extends JpaRepository<ParticipantsEntity, UUID> {
    // se tiene que actualizar porque si alguien se registra con otro documento, ya
    // sea pasaorte o cedula, esto pasara

    boolean existsByNroDocumento(String nroDocumento);

    Optional<ParticipantsEntity> findById(UUID id);

    Optional<ParticipantsEntity> findByNombres(String nombres);

}
