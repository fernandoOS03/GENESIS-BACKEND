package com.goodNews.genesis.services.validators;

import org.springframework.stereotype.Component;

import com.goodNews.genesis.dtos.travel.ViajeData;
import com.goodNews.genesis.exceptions.BadRequestException;
import com.goodNews.genesis.enums.MedTransporteEnum;

@Component
public class ValidadorBus implements ValidadorTransporteStrategy {

	@Override
	public void validar(ViajeData data) {

		if (data.empresaTransporte() == null || data.empresaTransporte().isBlank()
				|| data.lugarLlegada() == null || data.lugarLlegada().isBlank()
				|| data.fechaLlegada() == null
				|| data.fechaIda() == null
				|| data.boletoUrl() == null || data.boletoUrl().isBlank())
			throw new BadRequestException("Todos los datos del viaje son necesarios");
	}

	@Override
	public boolean aplicarPara(MedTransporteEnum tipoTransporte) {
		return MedTransporteEnum.TERRESTRE == tipoTransporte;
	}

}
