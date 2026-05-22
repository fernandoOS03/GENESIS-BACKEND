package com.goodNews.genesis.services.validators;

import com.goodNews.genesis.dtos.travel.ViajeData;
import com.goodNews.genesis.enums.MedTransporteEnum;

public interface ValidadorTransporteStrategy {
	void validar(ViajeData data);

	boolean aplicarPara(MedTransporteEnum tipoTransporte);

}
