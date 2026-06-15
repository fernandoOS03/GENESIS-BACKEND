package com.goodNews.genesis.modulos.viajes.entities;

import com.goodNews.genesis.modulos.participantes.entities.ParticipantsEntity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.goodNews.genesis.shared.enums.MedTransporteEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "informacion_viaje")
public class TravelInformationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "participante_id", referencedColumnName = "id")
    private ParticipantsEntity participante;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_transporte", length = 20)
    private MedTransporteEnum tipoTransporte;

    @Column(name = "empresa_transporte", length = 60)
    private String empresaTransporte;

    @Column(name = "nro_vuelo", length = 20)
    private String nroVuelo;
    
    @Column(name = "lugar_llegada", length = 100)
    private String lugarLlegada;
    
    @Column(name = "fecha_llegada")
    private LocalDateTime fechaLlegada;
    
    @Column(name = "fecha_ida")
    private LocalDateTime fechaIda;

    @Column(name = "foto_boleto_url", columnDefinition = "TEXT")
    private String boletoUrl;

}



