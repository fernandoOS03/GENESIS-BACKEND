package com.goodNews.genesis.modulos.viajes.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goodNews.genesis.modulos.viajes.entities.TravelInformationEntity;

@Repository
public interface TravelInformationRepository extends JpaRepository<TravelInformationEntity, UUID> {

    Optional<TravelInformationEntity> findByParticipanteId(UUID id);

}


