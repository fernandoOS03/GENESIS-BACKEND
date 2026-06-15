package com.goodNews.genesis.modulos.viajes.strategies;

import com.goodNews.genesis.modulos.viajes.dtos.ViajeData;
import com.goodNews.genesis.shared.enums.MedTransporteEnum;

public interface ValidadorTransporteStrategy {
	void validar(ViajeData data);

	boolean aplicarPara(MedTransporteEnum tipoTransporte);

}


