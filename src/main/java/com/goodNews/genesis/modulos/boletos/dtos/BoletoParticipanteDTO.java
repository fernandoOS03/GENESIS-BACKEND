package com.goodNews.genesis.modulos.boletos.dtos;

import java.util.UUID;

import com.goodNews.genesis.modulos.participantes.entities.ParticipantsEntity;


public record BoletoParticipanteDTO(

        UUID id,

        // Datos personales
        String nombres,
        String apellidos,
        String tipoDocumento,
        String nroDocumento,
        String sede,
        String pais,
        String rol,
        String nroBus,
        String hotel,
        String nroHabitacion,
        String maestro,
        String grupo

) {

    /**
     * Constructor de mapeo directo desde la entidad de participantes.
     * Los datos logísticos se inicializan con valores por defecto.
     */
    public BoletoParticipanteDTO(ParticipantsEntity entity,
            String nroBus, String hotel, String nroHabitacion,
            String maestro, String grupo) {
        this(
                entity.getId(),
                entity.getNombres(),
                entity.getApellidos(),
                entity.getTipoDocumento(),
                entity.getNroDocumento(),
                entity.getSede() != null ? entity.getSede().name().replace("_", " ") : "N/A",
                entity.getPais(),
                entity.getRol() != null ? entity.getRol().name() : "PARTICIPANTE",
                nroBus,
                hotel,
                nroHabitacion,
                maestro,
                grupo
        );
    }

    /**
     * Nombre completo para mostrar en el boleto: "APELLIDOS, Nombres"
     */
    public String nombreCompleto() {
        return apellidos().toUpperCase() + ", " + nombres();
    }

    /**
     * Nombre sugerido para el archivo PDF adjunto.
     */
    public String nombreArchivoPdf() {
        String apellidoSlug = apellidos().toUpperCase()
                .replace(" ", "-")
                .replaceAll("[^A-Z0-9\\-]", "");
        return "boleto-worldcamp2027-" + apellidoSlug + ".pdf";
    }
}
