package com.goodNews.genesis.modulos.pagos.repositories;

import com.goodNews.genesis.modulos.pagos.entities.AccountPayEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountPayRepository extends JpaRepository<AccountPayEntity, UUID> {
    Optional<AccountPayEntity> findByParticipanteId(UUID participanteId);
}
