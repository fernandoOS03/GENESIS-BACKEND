package com.goodNews.genesis.modulos.pagos.services;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.goodNews.genesis.core.exceptions.BadRequestException;
import com.goodNews.genesis.core.exceptions.ResourceNotFoundException;
import com.goodNews.genesis.modulos.pagos.dtos.AbonoRequestDTO;
import com.goodNews.genesis.modulos.pagos.dtos.AbonoResponseDTO;
import com.goodNews.genesis.modulos.pagos.entities.AccountPayEntity;
import com.goodNews.genesis.modulos.pagos.entities.TarifaConfigEntity;
import com.goodNews.genesis.modulos.pagos.entities.TransaccionEntity;
import com.goodNews.genesis.modulos.pagos.repositories.AccountPayRepository;
import com.goodNews.genesis.modulos.pagos.repositories.TarifaConfigRepository;
import com.goodNews.genesis.modulos.participantes.entities.ParticipantsEntity;
import com.goodNews.genesis.modulos.participantes.repositories.ParticipantRepository;
import com.goodNews.genesis.shared.enums.EstadoGeneralEnum;
import com.goodNews.genesis.shared.enums.EstadoPagoEnum;
import com.goodNews.genesis.shared.enums.MonedasEnum;
import com.goodNews.genesis.shared.events.ParticipanteConfirmadoEvent;
import org.springframework.context.ApplicationEventPublisher;

@Service
@RequiredArgsConstructor
public class PayService {

    private final TarifaConfigRepository tarifaConfigRepository;
    private final AccountPayRepository accountPayRepository;
    private final ParticipantRepository participantRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public AbonoResponseDTO registrarNuevoAbono(AbonoRequestDTO dto){
        AccountPayEntity cuenta = accountPayRepository.findById(dto.cuentaId())
                .orElseGet(() -> {
                    // Si no se encuentra por el ID de la cuenta de pago, se busca por el ID del participante
                    ParticipantsEntity participante = participantRepository.findById(dto.cuentaId()).orElse(null);
                    if (participante != null) {
                        return accountPayRepository.findByParticipanteId(participante.getId())
                                .orElseGet(() -> {
                                     AccountPayEntity nuevaCuenta = new AccountPayEntity();
                                     nuevaCuenta.setParticipante(participante);
                                     return accountPayRepository.save(nuevaCuenta);
                                });
                    }
                    return null;
                });

        if (cuenta == null) {
            throw new ResourceNotFoundException("Cuenta o Participante no encontrado con el ID: " + dto.cuentaId());
        }

        //Congelamos tarifa si es el primer pago
        if(cuenta.getTarifaCongelada() == null){
            MonedasEnum monedaPago;
            try {
                monedaPago = MonedasEnum.valueOf(dto.tipoMoneda());
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new BadRequestException("El tipo de moneda no es válido o está vacío: " + dto.tipoMoneda());
            }

            TarifaConfigEntity tarifaActual = tarifaConfigRepository.buscarTarifaVigente(LocalDate.now(), monedaPago)
                    .orElseThrow(() -> new ResourceNotFoundException("No hay tarifa configurada para la fecha actual en la moneda: " + monedaPago));

            cuenta.setTarifaCongelada(tarifaActual.getMonto());
            cuenta.setMonedaCongelada(monedaPago);
        } else {
            if (cuenta.getMonedaCongelada() == null) {
                // Si la tarifa ya está congelada pero la moneda congelada es null (registro antiguo),
                // inferimos la moneda basada en el monto (si > 500, PEN; de lo contrario, USD).
                MonedasEnum monedaInferida = cuenta.getTarifaCongelada() > 500.0 ? MonedasEnum.PEN : MonedasEnum.USD;
                cuenta.setMonedaCongelada(monedaInferida);
            }
            if(!cuenta.getMonedaCongelada().name().equals(dto.tipoMoneda())){
                throw new BadRequestException("Transacción rechazada. Esta cuenta está configurada para pagos exclusivamente en " + cuenta.getMonedaCongelada());
            }
        }

        //creamos la asociacion y la asociamos
        TransaccionEntity transaccion = new TransaccionEntity();
        transaccion.setMonto(dto.montoIngresado());
        transaccion.setTipoMoneda(dto.tipoMoneda());
        transaccion.setAccountPay(cuenta);

        if (cuenta.getTransacciones() == null) {
            cuenta.setTransacciones(new java.util.ArrayList<>());
        }
        cuenta.getTransacciones().add(transaccion);

        //Recalcular total y estado
        Double totalAbonado = cuenta.getTotalAbonado() != null ? cuenta.getTotalAbonado() : 0.0;
        Double nuevoTotal = totalAbonado + dto.montoIngresado();
        cuenta.setTotalAbonado(nuevoTotal);

        Double tarifaCongelada = cuenta.getTarifaCongelada() != null ? cuenta.getTarifaCongelada() : 0.0;
        boolean yaCompletado = cuenta.getEstado() == EstadoPagoEnum.COMPLETADO 
                || (cuenta.getParticipante() != null && cuenta.getParticipante().getEstadoPago() == EstadoPagoEnum.COMPLETADO);

        if (yaCompletado || nuevoTotal >= tarifaCongelada){
            cuenta.setEstado(EstadoPagoEnum.COMPLETADO);
            if (cuenta.getParticipante() != null) {
                EstadoGeneralEnum estadoAnterior = cuenta.getParticipante().getEstadoGeneral();
                cuenta.getParticipante().setEstadoPago(EstadoPagoEnum.COMPLETADO);
                cuenta.getParticipante().actualizarEstadoGeneral();
                participantRepository.save(cuenta.getParticipante());
                
                if (estadoAnterior != EstadoGeneralEnum.CONFIRMADO && 
                    cuenta.getParticipante().getEstadoGeneral() == EstadoGeneralEnum.CONFIRMADO) {
                    applicationEventPublisher.publishEvent(
                        new ParticipanteConfirmadoEvent(this, cuenta.getParticipante().getId())
                    );
                }
            }
        } else {
            cuenta.setEstado(EstadoPagoEnum.PARCIAL);
            if (cuenta.getParticipante() != null) {
                cuenta.getParticipante().setEstadoPago(EstadoPagoEnum.PARCIAL);
                cuenta.getParticipante().actualizarEstadoGeneral();
                participantRepository.save(cuenta.getParticipante());
            }
        }

        //calculamos porcentaje pagado
        Double porcentaje = tarifaCongelada > 0 ? (nuevoTotal / tarifaCongelada * 100) : 0.0;

        accountPayRepository.save(cuenta);
        String estadoName = cuenta.getEstado() != null ? cuenta.getEstado().name() : EstadoPagoEnum.PENDIENTE.name();
        return  new AbonoResponseDTO(estadoName,  cuenta.getTotalAbonado(), cuenta.getTarifaCongelada(), porcentaje);
    }
}
