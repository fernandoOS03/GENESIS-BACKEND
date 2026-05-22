package com.goodNews.genesis.dtos.travel;

import java.time.LocalDateTime;
import com.goodNews.genesis.enums.MedTransporteEnum;

public interface ViajeData {
    MedTransporteEnum tipoTransporte();

    String empresaTransporte();

    String nroVuelo();

    LocalDateTime fechaLlegada();

    LocalDateTime fechaIda();

    String boletoUrl();

    String lugarLlegada();
}