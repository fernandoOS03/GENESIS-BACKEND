package com.goodNews.genesis.modulos.boletos.listeners;

import com.goodNews.genesis.modulos.boletos.services.BoletoService;
import com.goodNews.genesis.shared.events.ParticipanteConfirmadoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoletoEventListener {

    private final BoletoService boletoService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleParticipanteConfirmadoEvent(ParticipanteConfirmadoEvent event) {
        log.info("Evento recibido: Participante confirmado {}. Iniciando envío de boleto PDF...", event.getParticipanteId());
        try {
            boletoService.enviarBoletoIndividual(event.getParticipanteId());
            log.info("Boleto enviado exitosamente para el participante {}", event.getParticipanteId());
        } catch (Exception e) {
            log.error("Error al enviar el boleto automáticamente para el participante {}: {}", event.getParticipanteId(), e.getMessage(), e);
        }
    }
}
