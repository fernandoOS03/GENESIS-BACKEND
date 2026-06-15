package com.goodNews.genesis.modulos.viajes.dtos;

import java.time.LocalDateTime;
import com.goodNews.genesis.shared.enums.MedTransporteEnum;

public interface ViajeData {
    MedTransporteEnum tipoTransporte();

    String empresaTransporte();

    String nroVuelo();

    LocalDateTime fechaLlegada();

    LocalDateTime fechaIda();

    String boletoUrl();

    String lugarLlegada();
}

