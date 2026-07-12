package com.goodNews.genesis.modulos.grupos.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.goodNews.genesis.modulos.participantes.entities.ParticipantsEntity;
import com.goodNews.genesis.shared.enums.GenerosEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "grupos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrupoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(length = 100, nullable = false)
    private String nombre;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private GenerosEnum genero;

    @NotNull
    @Column(name = "edad_minima", nullable = false)
    private Integer edadMinima;

    @NotNull
    @Column(name = "edad_maxima", nullable = false)
    private Integer edadMaxima;

    @NotNull
    @Column(name = "capacidad_max", nullable = false)
    private Integer capacidadMax = 5;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maestro_id")
    @ToString.Exclude
    private ParticipantsEntity maestro;

    // Relación inversa: todos los participantes cuyo grupo_id apunta aquí.
    // Incluye al maestro (su grupo_id también se setea a este grupo al crearlo).
    @OneToMany(mappedBy = "grupo", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<ParticipantsEntity> participantes = new ArrayList<>();
}
