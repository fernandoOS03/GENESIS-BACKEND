package com.goodNews.genesis.modulos.viajes.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

import com.goodNews.genesis.shared.enums.MedTransporteEnum;

public record ActualizarViajeDTO(
		UUID id,
		MedTransporteEnum tipoTransporte,
		String empresaTransporte,
		String nroVuelo,
		String lugarLlegada,
		LocalDateTime fechaLlegada,
		LocalDateTime fechaIda,
		String boletoUrl) implements ViajeData {
}

