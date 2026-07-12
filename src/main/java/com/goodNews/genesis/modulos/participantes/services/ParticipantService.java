package com.goodNews.genesis.modulos.participantes.services;


import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.goodNews.genesis.modulos.viajes.dtos.ActualizarViajeDTO;
import com.goodNews.genesis.modulos.viajes.dtos.VerificarViajeDTO;
import com.goodNews.genesis.modulos.viajes.dtos.VerificacionResponseDTO;
import com.goodNews.genesis.modulos.participantes.dtos.ParticipanteAdminDTO;
import com.goodNews.genesis.modulos.participantes.dtos.ParticipanteRegistroDTO;
import com.goodNews.genesis.modulos.participantes.dtos.ParticipanteResponseDTO;
import com.goodNews.genesis.modulos.pagos.entities.AccountPayEntity;
import com.goodNews.genesis.modulos.participantes.entities.ParticipantsEntity;
import com.goodNews.genesis.modulos.viajes.entities.TravelInformationEntity;
import com.goodNews.genesis.shared.enums.CondParticipanteEnum;
import com.goodNews.genesis.shared.enums.EstadoGeneralEnum;
import com.goodNews.genesis.shared.enums.EstadoTransporteEnum;
import com.goodNews.genesis.shared.events.ParticipanteConfirmadoEvent;
import org.springframework.context.ApplicationEventPublisher;
import com.goodNews.genesis.core.exceptions.BadRequestException;
import com.goodNews.genesis.core.exceptions.ResourceNotFoundException;
import com.goodNews.genesis.core.security.SecurityHelper;
import com.goodNews.genesis.core.security.CustomUserDetails;
import com.goodNews.genesis.modulos.participantes.repositories.ParticipantRepository;
import com.goodNews.genesis.modulos.viajes.repositories.TravelInformationRepository;
import com.goodNews.genesis.modulos.viajes.strategies.ValidadorTransporteStrategy;
import com.goodNews.genesis.shared.services.EmailService;
import com.goodNews.genesis.shared.services.JwtService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ParticipantService {

	@Value("${frontend.url.registro.viaje}")
	private String urlToUpdate;

	private final ParticipantRepository participantRepository;
	private final TravelInformationRepository travelInforParticipantRepository;
	private final List<ValidadorTransporteStrategy> validadores;
	private final EmailService emailService;

	private final TemplateEngine templateEngine;

	private final SecurityHelper securityHelper;
	private final ApplicationEventPublisher applicationEventPublisher;



	// ==========================================================
	// Admin: Listar todos los participantes
	// ==========================================================
	public List<ParticipanteAdminDTO> listarTodos() {
		CustomUserDetails usuarioActual = securityHelper.getCurrentUser();
		if (usuarioActual == null) {
			throw new BadRequestException("Usuario no autenticado");
		}

		if (securityHelper.isSuperAdmin()) {
			return participantRepository.findAll()
					.stream()
					.map(ParticipanteAdminDTO::new)
					.toList();
		} else {
			String paisUsuario = usuarioActual.getPais();
			if (paisUsuario == null || paisUsuario.isBlank()) {
				throw new BadRequestException("El usuario no tiene un país asignado");
			}
			return participantRepository.findByPaisIgnoreCase(paisUsuario)
					.stream()
					.map(ParticipanteAdminDTO::new)
					.toList();
		}
	}

	// ==========================================================
	// REGISTRO PÚBLICO: El participante se registra a sí mismo
	// (sin token, sin validación de sesión)
	// ==========================================================
	public ParticipanteResponseDTO registrarParticipante(ParticipanteRegistroDTO dto) {

		boolean existeParticipante = participantRepository.existsByNroDocumento(dto.nroDocumento());
		if (existeParticipante) {
			throw new BadRequestException("El participante ya esta registrado");
		}

		return guardarParticipante(dto);
	}

	// ==========================================================
	// REGISTRO ADMIN: Un admin/editor crea un participante desde su panel
	// (requiere token, valida que el país del participante coincida con el del admin)
	// ==========================================================
	public ParticipanteResponseDTO registrarParticipanteDesdeAdmin(ParticipanteRegistroDTO dto) {

		CustomUserDetails usuarioActual = securityHelper.getCurrentUser();
		if (usuarioActual == null) {
			throw new BadRequestException("Usuario no autenticado");
		}

		// COUNTRY_ADMIN y EDITOR solo pueden registrar participantes de su país
		if (!securityHelper.canAccessCountry(dto.pais())) {
			throw new BadRequestException("No tienes permisos para registrar participantes en " + dto.pais());
		}

		boolean existeParticipante = participantRepository.existsByNroDocumento(dto.nroDocumento());
		if (existeParticipante) {
			throw new BadRequestException("El participante ya esta registrado");
		}

		return guardarParticipante(dto);
	}

	// ==========================================================
	// Lógica compartida de guardado (usada por ambos registros)
	// ==========================================================
	private ParticipanteResponseDTO guardarParticipante(ParticipanteRegistroDTO dto) {

		ParticipantsEntity participante = new ParticipantsEntity();
		participante.setTipoDocumento(dto.tipoDocumento());
		participante.setNroDocumento(dto.nroDocumento());
		participante.setNombres(dto.nombres());
		participante.setApellidos(dto.apellidos());
		participante.setEmail(dto.email());
		participante.setPais(dto.pais());
		participante.setSede(dto.sede());
		participante.setTelefono(dto.telefono());
		participante.setGenero(dto.genero());
		participante.setFechaNacimiento(dto.fechaNacimiento());
		participante.setTallaPolo(dto.tallaPolo());
		participante.setCondParticipacion(dto.condParticipacion());
		participante.setRol(dto.rol());

		// Estamos aplicando el principio de fail fast (falla rapido), es mejor lanzar
		// una excepcion si la condicion prohibida se cumple en vez de envolver el registro en un if

		// boolean esDeLima = "Peru".equalsIgnoreCase(dto.pais()) &&
		// "Lima".equalsIgnoreCase(dto.sede());

		boolean esDeLima = "PE".equalsIgnoreCase(dto.pais()) && dto.sede() != null && dto.sede().isLima();

		if (esDeLima && dto.tipoTransporte() != null) {
			throw new BadRequestException("Los participantes de la sede Lima no requieren registro de transporte.");
		}

		if (CondParticipanteEnum.MIEMBRO == dto.condParticipacion() && dto.rol() == null) {
			throw new BadRequestException("Debe ingresar un rol");
		}

		AccountPayEntity accountPay = new AccountPayEntity();
		accountPay.setParticipante(participante);
		participante.setAccountPay(accountPay);

		participante = participantRepository.save(participante);

		// URL fija sin token — el acceso se protege con nroDocumento + codigoViaje
		String urlActualizarRegistro = urlToUpdate;

		// Se hace esto para enviar a thymeleaf
		// Diciendolo que se le asigne el valor de la variable de ese nombre y pueda usarse
		Context contexto = new Context();
		contexto.setVariable("esDeLima", esDeLima);
		contexto.setVariable("urlUpdateRegistro", urlActualizarRegistro);
		contexto.setVariable("codigoViaje", participante.getCodigoViaje());
		contexto.setVariable("nombreParticipante", participante.getNombres());
		contexto.setVariable("participante", participante);

		String cuerpoHtml = templateEngine.process("correo-preinscripcion", contexto);
		String asunto = "Confirmación de Inscripción";

		emailService.sendEmail(participante.getEmail(), asunto, cuerpoHtml);

		if (dto.tipoTransporte() != null) {
			registrarViaje(participante, dto);
		}

		return new ParticipanteResponseDTO(participante);
	}

	private void registrarViaje(ParticipantsEntity participante, ParticipanteRegistroDTO dto) {

		// Aplicamos la validacion de los datos del viaje, llenar si se selcciono uno o
		// ignorar si no hay nada
		if (tieneDatosViaje(dto)) {
			boolean validadorEncontrado = false;
			// Este bucle nos sirve para definir cual validador usara cada case segun lo que
			// el usuario envie
			for (ValidadorTransporteStrategy validador : validadores) {
				if (validador.aplicarPara(dto.tipoTransporte())) {
					validador.validar(dto);
					validadorEncontrado = true;
					break;
				}
			}

			if (!validadorEncontrado) {
				throw new BadRequestException("El tipo de transporte no es valido");
			}
		}

		TravelInformationEntity infoViaje = new TravelInformationEntity();

		infoViaje.setParticipante(participante);
		infoViaje.setTipoTransporte(dto.tipoTransporte());
		infoViaje.setEmpresaTransporte(dto.empresaTransporte());
		infoViaje.setNroVuelo(dto.nroVuelo());
		infoViaje.setFechaLlegada(dto.fechaLlegada());
		infoViaje.setFechaIda(dto.fechaIda());
		infoViaje.setBoletoUrl(dto.boletoUrl());
		infoViaje.setLugarLlegada(dto.lugarLlegada());

		infoViaje = travelInforParticipantRepository.save(infoViaje);

		EstadoGeneralEnum estadoAnterior = participante.getEstadoGeneral();
		participante.setEstadoTransporte(EstadoTransporteEnum.ASIGNADO);
		participante.actualizarEstadoGeneral();

		if (estadoAnterior != EstadoGeneralEnum.CONFIRMADO && 
			participante.getEstadoGeneral() == EstadoGeneralEnum.CONFIRMADO) {
			applicationEventPublisher.publishEvent(
				new ParticipanteConfirmadoEvent(this, participante.getId())
			);
		}
	}

	// ==========================================================
	// Verificar código de viaje (check-in público)
	// ==========================================================
	public VerificacionResponseDTO verificarCodigoViaje(VerificarViajeDTO dto) {
		ParticipantsEntity participante = participantRepository
				.findByNroDocumentoAndCodigoViaje(dto.nroDocumento(), dto.codigoViaje().toUpperCase())
				.orElseThrow(() -> new BadRequestException("Número de documento o código incorrecto"));
		return new VerificacionResponseDTO(participante.getId(), participante.getNombres());
	}

	public ActualizarViajeDTO actualizarViajeParticipante(ActualizarViajeDTO dto, UUID id) {

		boolean validadorEncontrado = false;
		for (ValidadorTransporteStrategy validador : validadores) {
			if (validador.aplicarPara(dto.tipoTransporte())) {
				validador.validar(dto);
				validadorEncontrado = true;
				break;

			}
		}
		if (!validadorEncontrado) {
			throw new BadRequestException("El tipo de transporte no es valido");
		}

		// Lo que hacemos aqui es un upsert que basicamente se encarga de validar si el
		// participante existe, en caso de que si lo recuperamos para editar su vuelo,
		// en caso de que no lo crea y se le asigna los nuevos datos
		ParticipantsEntity participanteExistente = participantRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("El participante no existe"));

		// Validar permisos si el que edita es un administrador o editor autenticado en el sistema
		CustomUserDetails usuarioActual = securityHelper.getCurrentUser();
		if (usuarioActual != null && usuarioActual.getRol() != com.goodNews.genesis.shared.enums.UsersEnum.ROLE_SUPER_ADMIN) {
			if (!securityHelper.canAccessCountry(participanteExistente.getPais())) {
				throw new BadRequestException("No tienes permisos para editar participantes de " + participanteExistente.getPais());
			}
		}

		TravelInformationEntity infoViaje = travelInforParticipantRepository.findByParticipanteId(id)
				.orElseGet(() -> {
					TravelInformationEntity nuevoViaje = new TravelInformationEntity();
					nuevoViaje.setParticipante(participanteExistente);
					return nuevoViaje;
				});

		infoViaje.setTipoTransporte(dto.tipoTransporte());
		infoViaje.setEmpresaTransporte(dto.empresaTransporte());
		infoViaje.setNroVuelo(dto.nroVuelo());
		infoViaje.setFechaLlegada(dto.fechaLlegada());
		infoViaje.setFechaIda(dto.fechaIda());
		infoViaje.setBoletoUrl(dto.boletoUrl());
		infoViaje.setLugarLlegada(dto.lugarLlegada());

		TravelInformationEntity datosGuardados = travelInforParticipantRepository.save(infoViaje);

		EstadoGeneralEnum estadoAnterior = participanteExistente.getEstadoGeneral();
		participanteExistente.setEstadoTransporte(EstadoTransporteEnum.ASIGNADO);
		participanteExistente.actualizarEstadoGeneral();
		participantRepository.save(participanteExistente);

		if (estadoAnterior != EstadoGeneralEnum.CONFIRMADO && 
			participanteExistente.getEstadoGeneral() == EstadoGeneralEnum.CONFIRMADO) {
			applicationEventPublisher.publishEvent(
				new ParticipanteConfirmadoEvent(this, participanteExistente.getId())
			);
		}

		// Este contexto me sirve para llenar los datos en el envio de correos
		Context contexto = new Context();
		contexto.setVariable("nombreParticipante", datosGuardados.getParticipante().getNombres());
		contexto.setVariable("dataSave", datosGuardados);

		String cuerpoHtml = templateEngine.process("correo-actualizacion-transporte", contexto);
		String asunto = "Confirmación de Transporte";

		emailService.sendEmail(datosGuardados.getParticipante().getEmail(), asunto, cuerpoHtml);

		return new ActualizarViajeDTO(
				datosGuardados.getId(),
				datosGuardados.getTipoTransporte(),
				datosGuardados.getEmpresaTransporte(),
				datosGuardados.getNroVuelo(),
				datosGuardados.getLugarLlegada(),
				datosGuardados.getFechaLlegada(),
				datosGuardados.getFechaIda(),
				datosGuardados.getBoletoUrl());

	}

	// Este metodo me sirve para validar si el participante ya selecciono algun dato
	// de viaje y obligara a lllenar todos en caso de que no
	// Si puede enviar null
	private boolean tieneDatosViaje(ParticipanteRegistroDTO dto) {
		return dto.tipoTransporte() != null
				|| (dto.empresaTransporte() != null && !dto.empresaTransporte().isBlank())
				|| (dto.nroVuelo() != null && !dto.nroVuelo().isBlank())
				|| dto.fechaLlegada() != null
				|| dto.fechaIda() != null
				|| (dto.boletoUrl() != null && !dto.boletoUrl().isBlank())
				|| (dto.lugarLlegada() != null && !dto.lugarLlegada().isBlank());
	}

}


