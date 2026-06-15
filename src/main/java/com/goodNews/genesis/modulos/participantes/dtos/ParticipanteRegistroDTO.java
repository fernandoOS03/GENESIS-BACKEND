package com.goodNews.genesis.modulos.participantes.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.goodNews.genesis.modulos.viajes.dtos.ViajeData;
import com.goodNews.genesis.shared.enums.CargosEnum;
import com.goodNews.genesis.shared.enums.CondParticipanteEnum;
import com.goodNews.genesis.shared.enums.GenerosEnum;
import com.goodNews.genesis.shared.enums.MedTransporteEnum;
import com.goodNews.genesis.shared.enums.SedesEnum;
import com.goodNews.genesis.shared.enums.TallaPolosEnum;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ParticipanteRegistroDTO(
        @NotBlank(message = "El tipo de documento es obligatorio") String tipoDocumento,

        @NotBlank(message = "El numero de documento es obligatorio") @Size(min = 8, max = 20) String nroDocumento,

        @NotBlank(message = "El nombre es obligatorio") String nombres,

        @NotBlank(message = "Los Apellidos son obligatorios") String apellidos,

        @NotBlank(message = "El email es obligatorio") @Email String email,

        @NotNull(message = "El genero es obligatorio") GenerosEnum genero,

        @NotBlank(message = "El pais es obligatorio") @Pattern(regexp = "^[A-Z]{2}$", message = "Código de país inválido") String pais,

        SedesEnum sede,

        String telefono,

        @NotNull(message = "Fecha nacimiento obligatoria") @Past LocalDate fechaNacimiento,

        @NotNull(message = "La talla del polo es obligatoria") TallaPolosEnum tallaPolo,

        @NotNull(message = "La talla del polo es obligatoria") CondParticipanteEnum condParticipacion,

        CargosEnum rol,

        MedTransporteEnum tipoTransporte,

        String empresaTransporte,

        String nroVuelo,

        LocalDateTime fechaLlegada,

        LocalDateTime fechaIda,

        String boletoUrl,

        String lugarLlegada

) implements ViajeData {

}


