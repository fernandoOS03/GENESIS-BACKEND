package com.goodNews.genesis.modulos.participantes.entities;

import com.goodNews.genesis.modulos.viajes.entities.TravelInformationEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.goodNews.genesis.shared.enums.CargosEnum;
import com.goodNews.genesis.shared.enums.CondParticipanteEnum;
import com.goodNews.genesis.shared.enums.EstadosEnum;
import com.goodNews.genesis.shared.enums.GenerosEnum;
import com.goodNews.genesis.shared.enums.SedesEnum;
import com.goodNews.genesis.shared.enums.TallaPolosEnum;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "participantes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantsEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@NotBlank
	@Column(name = "tipo_documento", nullable = false, length = 20)
	private String tipoDocumento;

	@NotBlank
	@Column(name = "nmro_documento", unique = true, length = 20, nullable = false)
	private String nroDocumento;

	@NotBlank
	@Column(length = 100, nullable = false)
	private String nombres;

	@NotBlank
	@Column(length = 100, nullable = false)
	private String apellidos;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(length = 20, nullable = false)
	private GenerosEnum genero;

	@NotNull
	@Column(name = "fecha_nacimiento", nullable = false)
	private LocalDate fechaNacimiento;

	@NotBlank
	@Email
	@Column(length = 100, nullable = false)
	private String email;

	@NotBlank
	@Column(length = 20)
	private String telefono;

	@NotBlank
	@Column(length = 30, nullable = false)
	private String pais;

	@Enumerated(EnumType.STRING)
	@Column(length = 50)
	private SedesEnum sede;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "talla_polo", length = 5, nullable = false)
	private TallaPolosEnum tallaPolo;

	@NotNull
	@Enumerated(EnumType.STRING) // Esto porque los enum en la db son representados con 0 para la primera opcion
									// 1 para la segunda, esto guarda el texto plano
	@Column(name = "condicion_participacion", length = 10)
	private CondParticipanteEnum condParticipacion;

	@Enumerated(EnumType.STRING)
	@Column(name = "rol")
	private CargosEnum rol;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado_registro", length = 30)
	private EstadosEnum estadoRegistro;

	@Column(name = "fecha_registro")
	private LocalDateTime fechaRegistro;

	// Relacion con informacion de viaje
	@OneToOne(mappedBy = "participante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private TravelInformationEntity travelInformation;

	@OneToOne(mappedBy = "participante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private AccountPayEntity accountPay;

	// Esto asigna la fecha actual al registrar el participante
	// Ademas se le asigna un estado por defecto en caso no se asigne uno
	@PrePersist
	public void onCreate() {
		this.fechaRegistro = LocalDateTime.now();
		if (estadoRegistro == null) {
			this.estadoRegistro = EstadosEnum.PRE_INSCRITO;
		}
	}

}



