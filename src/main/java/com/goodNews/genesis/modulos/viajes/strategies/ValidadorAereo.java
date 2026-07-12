package com.goodNews.genesis.modulos.viajes.strategies;

import org.springframework.stereotype.Component;

import com.goodNews.genesis.modulos.viajes.dtos.ViajeData;
import com.goodNews.genesis.core.exceptions.BadRequestException;
import com.goodNews.genesis.shared.enums.MedTransporteEnum;

@Component
public class ValidadorAereo implements ValidadorTransporteStrategy {

	@Override
	public void validar(ViajeData data) {

		if (data.empresaTransporte() == null || data.empresaTransporte().isBlank()
				|| data.nroVuelo() == null || data.nroVuelo().isBlank()
				|| data.fechaLlegada() == null
				|| data.fechaIda() == null) {
			throw new BadRequestException("Todos los datos del vuelo son obligatorios");
		}
	}

	@Override
	public boolean aplicarPara(MedTransporteEnum tipoTransporte) {
		return MedTransporteEnum.AEREO == tipoTransporte;
	}

}


