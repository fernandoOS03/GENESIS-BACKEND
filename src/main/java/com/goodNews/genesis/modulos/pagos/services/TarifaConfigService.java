package com.goodNews.genesis.modulos.pagos.services;

import com.goodNews.genesis.core.exceptions.BadRequestException;
import com.goodNews.genesis.modulos.pagos.dtos.TarifaRequestDTO;
import com.goodNews.genesis.modulos.pagos.dtos.TarifaResponseDTO;
import com.goodNews.genesis.modulos.pagos.entities.TarifaConfigEntity;
import com.goodNews.genesis.modulos.pagos.repositories.TarifaConfigRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TarifaConfigService {

    private final TarifaConfigRepository repository;

    public TarifaResponseDTO crearTarifa(TarifaRequestDTO dto) {
        if (dto.fechaFin().isBefore(dto.fechaInicio())) {
            throw new BadRequestException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        TarifaConfigEntity entity = new TarifaConfigEntity();
        entity.setMonto(dto.monto());
        entity.setFechaInicio(dto.fechaInicio());
        entity.setFechaFin(dto.fechaFin());
        entity.setMoneda(dto.moneda());
        TarifaConfigEntity saved = repository.save(entity);
        return new TarifaResponseDTO(saved);
    }

    public List<TarifaResponseDTO> listarTodas() {
        return repository.findAll().stream()
                .map(TarifaResponseDTO::new)
                .toList();
    }
}
