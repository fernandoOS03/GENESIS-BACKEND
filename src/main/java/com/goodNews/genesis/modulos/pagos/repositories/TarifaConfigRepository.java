package com.goodNews.genesis.modulos.pagos.repositories;

import com.goodNews.genesis.modulos.pagos.entities.TarifaConfigEntity;
import com.goodNews.genesis.shared.enums.MonedasEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface TarifaConfigRepository extends JpaRepository<TarifaConfigEntity, UUID> {
    @Query("SELECT t FROM TarifaConfigEntity t where :fechaActual BETWEEN t.fechaInicio AND t.fechaFin AND t.moneda = :moneda")
    Optional<TarifaConfigEntity> buscarTarifaVigente(@Param("fechaActual") LocalDate fechaActual, @Param("moneda") MonedasEnum moneda);
}
