package com.goodNews.genesis.modulos.participantes.entities;

import com.goodNews.genesis.modulos.viajes.entities.TravelInformationEntity;
import com.goodNews.genesis.modulos.pagos.entities.AccountPayEntity;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.goodNews.genesis.shared.enums.CargosEnum;
import com.goodNews.genesis.shared.enums.CondParticipanteEnum;
import com.goodNews.genesis.shared.enums.EstadoGeneralEnum;
import com.goodNews.genesis.shared.enums.EstadoPagoEnum;
import com.goodNews.genesis.shared.enums.EstadoTransporteEnum;
import com.goodNews.genesis.shared.enums.GenerosEnum;
import com.goodNews.genesis.shared.enums.SedesEnum;
import com.goodNews.genesis.shared.enums.TallaPolosEnum;

import com.goodNews.genesis.modulos.grupos.entities.GrupoEntity;

import jakarta.persistence.CascadeType;
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
	@Column(name = "estado_pago", length = 30)
	private EstadoPagoEnum estadoPago;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado_transporte", length = 30)
	private EstadoTransporteEnum estadoTransporte;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado_general", length = 30)
	private EstadoGeneralEnum estadoGeneral;

	@Column(name = "fecha_registro")
	private LocalDateTime fechaRegistro;

	@Column(name = "codigo_viaje", length = 5)
	private String codigoViaje;

	// Relacion con informacion de viaje
	@OneToOne(mappedBy = "participante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private TravelInformationEntity travelInformation;

	@OneToOne(mappedBy = "participante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private AccountPayEntity accountPay;

	// Relacion con el grupo asignado (nullable: null = sin grupo aún)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "grupo_id")
	private GrupoEntity grupo;

	// Esto asigna la fecha actual al registrar el participante
	// Ademas se le asigna un estado por defecto en caso no se asigne uno
	@PrePersist
	public void onCreate() {
		this.fechaRegistro = LocalDateTime.now();
		if (this.codigoViaje == null) {
			this.codigoViaje = generarCodigoViaje();
		}
		if (this.estadoGeneral == null) {
			this.estadoGeneral = EstadoGeneralEnum.PRE_INSCRITO;
		}
		if (this.estadoPago == null) {
			this.estadoPago = EstadoPagoEnum.PENDIENTE;
		}
		if (this.estadoTransporte == null) {
			if (this.sede != null && this.sede.isLima()) {
				this.estadoTransporte = EstadoTransporteEnum.NO_APLICA;
			} else {
				this.estadoTransporte = EstadoTransporteEnum.PENDIENTE;
			}
		}
		actualizarEstadoGeneral();
	}

	public void actualizarEstadoGeneral() {
		if (this.sede == null || this.estadoPago == null || this.estadoTransporte == null) {
			return;
		}
		if (this.sede.isLima() && this.estadoPago == EstadoPagoEnum.COMPLETADO) {
			this.estadoGeneral = EstadoGeneralEnum.CONFIRMADO;
		} else if (!this.sede.isLima() && this.estadoPago == EstadoPagoEnum.COMPLETADO && this.estadoTransporte == EstadoTransporteEnum.ASIGNADO) {
			this.estadoGeneral = EstadoGeneralEnum.CONFIRMADO;
		} else {
			this.estadoGeneral = EstadoGeneralEnum.PRE_INSCRITO;
		}
	}

	private String generarCodigoViaje() {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		SecureRandom random = new SecureRandom();
		StringBuilder sb = new StringBuilder(5);
		for (int i = 0; i < 5; i++) {
			sb.append(chars.charAt(random.nextInt(chars.length())));
		}
		return sb.toString();
	}

}



