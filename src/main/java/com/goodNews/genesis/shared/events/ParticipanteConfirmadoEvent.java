package com.goodNews.genesis.shared.events;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

public class ParticipanteConfirmadoEvent extends ApplicationEvent {
    
    private final UUID participanteId;

    public ParticipanteConfirmadoEvent(Object source, UUID participanteId) {
        super(source);
        this.participanteId = participanteId;
    }

    public UUID getParticipanteId() {
        return participanteId;
    }
}
