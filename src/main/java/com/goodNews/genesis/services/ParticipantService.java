package com.goodNews.genesis.services;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.goodNews.genesis.dtos.travel.ActualizarViajeDTO;
import com.goodNews.genesis.dtos.participant.ParticipanteAdminDTO;
import com.goodNews.genesis.dtos.participant.ParticipanteRegistroDTO;
import com.goodNews.genesis.dtos.participant.ParticipanteResponseDTO;
import com.goodNews.genesis.entities.ParticipantsEntity;
import com.goodNews.genesis.entities.TravelInformationEntity;
import com.goodNews.genesis.enums.CondParticipanteEnum;
import com.goodNews.genesis.enums.SedesEnum;
import com.goodNews.genesis.exceptions.BadRequestException;
import com.goodNews.genesis.exceptions.ResourceNotFoundException;
import com.goodNews.genesis.repository.ParticipantRepository;
import com.goodNews.genesis.repository.TravelInformationRepository;
import com.goodNews.genesis.services.validators.ValidadorTransporteStrategy;
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

	@Autowired
	private JwtService jwtService;

	// Esto para que si selecciona cualquier sede de lima no pueda acceder al valor
	// transporte
	private static final EnumSet<SedesEnum> SEDES_LIMA = EnumSet.of(SedesEnum.LIMA_CENTRO, SedesEnum.VENTANILLA,
			SedesEnum.VILLA_SALVADOR, SedesEnum.HUAYCAN, SedesEnum.MANCHAY);

	// ==========================================================
	// Admin: Listar todos los participantes
	// ==========================================================
	public List<ParticipanteAdminDTO> listarTodos() {
		return participantRepository.findAll()
				.stream()
				.map(ParticipanteAdminDTO::new)
				.toList();
	}

	public ParticipanteResponseDTO registrarParticipante(ParticipanteRegistroDTO dto) {

		boolean existeParticipante = participantRepository.existsByNroDocumento(dto.nroDocumento());

		if (existeParticipante) {
			throw new BadRequestException("El participante ya esta registrado");
		}

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
		// una excepcion si la condcion
		// prohibida se cumple en vez de envolver el registro en un if

		// boolean esDeLima = "Peru".equalsIgnoreCase(dto.pais()) &&
		// "Lima".equalsIgnoreCase(dto.sede());

		boolean esDeLima = "PE".equalsIgnoreCase(dto.pais()) && SEDES_LIMA.contains(dto.sede());

		if (esDeLima && dto.tipoTransporte() != null) {
			throw new BadRequestException("Los participantes de la sede Lima no requieren registro de transporte.");
		}

		if (CondParticipanteEnum.MIEMBRO == dto.condParticipacion() && dto.rol() == null) {
			throw new BadRequestException("Debe ingresar un rol");
		}

		participante = participantRepository.save(participante);

		// Generamos un token para el participante
		String token = jwtService.generateToken(participante.getId().toString());

		// Esta url me servira para completar el registro de los participantes
		// que no pudieron llenar los datos de su vuelo en el preregistro
		String urlUpdateRegistro = urlToUpdate + "?token=" + token;

		// Se hace esto para enviar a thymeleaf
		// Diciendolo que se le asigne el valor de la variable de ese nombre y pueda
		// usarse
		Context context = new Context();
		context.setVariable("esDeLima", esDeLima);
		context.setVariable("urlUpdateRegistro", urlUpdateRegistro);
		context.setVariable("nombreParticipante", participante.getNombres());
		context.setVariable("participante", participante);

		String bodyHtml = templateEngine.process("correo-preinscripcion", context);
		String asunto = "Confirmación de Inscripción";

		emailService.sendEmail(participante.getEmail(), asunto, bodyHtml);

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
			// el usuari oenvie
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

		TravelInformationEntity travelInfo = new TravelInformationEntity();

		travelInfo.setParticipante(participante);
		travelInfo.setTipoTransporte(dto.tipoTransporte());
		travelInfo.setEmpresaTransporte(dto.empresaTransporte());
		travelInfo.setNroVuelo(dto.nroVuelo());
		travelInfo.setFechaLlegada(dto.fechaLlegada());
		travelInfo.setFechaIda(dto.fechaIda());
		travelInfo.setBoletoUrl(dto.boletoUrl());
		travelInfo.setLugarLlegada(dto.lugarLlegada());

		travelInfo = travelInforParticipantRepository.save(travelInfo);

	}

	public ActualizarViajeDTO updateParticipantTravel(ActualizarViajeDTO dto, UUID id) {

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

		// LO que hacemos aqui es un upserte que basicamente se encarga de validar si el
		// participante existe, en caso de que s
		// lo recuperamaos para editar su vuelo en caso de que no lo crea y se le asigna
		// los nuevos datos
		ParticipantsEntity existParticipant = participantRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("El participante no existe"));

		TravelInformationEntity travelInformation = travelInforParticipantRepository.findByParticipanteId(id)
				.orElseGet(() -> {
					TravelInformationEntity newTravel = new TravelInformationEntity();
					newTravel.setParticipante(existParticipant);
					return newTravel;
				});

		travelInformation.setTipoTransporte(dto.tipoTransporte());
		travelInformation.setEmpresaTransporte(dto.empresaTransporte());
		travelInformation.setNroVuelo(dto.nroVuelo());
		travelInformation.setFechaLlegada(dto.fechaLlegada());
		travelInformation.setFechaIda(dto.fechaIda());
		travelInformation.setBoletoUrl(dto.boletoUrl());
		travelInformation.setLugarLlegada(dto.lugarLlegada());

		TravelInformationEntity dataSave = travelInforParticipantRepository.save(travelInformation);

		// Ester context me sirve para llenar los datos en el envio de correos
		Context context = new Context();
		context.setVariable("nombreParticipante", dataSave.getParticipante().getNombres());
		context.setVariable("dataSave", dataSave);

		String bodyHtml = templateEngine.process("correo-actualizacion-transporte", context);
		String asunto = "Confirmación de Transporte";

		emailService.sendEmail(dataSave.getParticipante().getEmail(), asunto, bodyHtml);

		return new ActualizarViajeDTO(
				dataSave.getId(),
				dataSave.getTipoTransporte(),
				dataSave.getEmpresaTransporte(),
				dataSave.getNroVuelo(),
				dataSave.getLugarLlegada(),
				dataSave.getFechaLlegada(),
				dataSave.getFechaIda(),
				dataSave.getBoletoUrl());

	}

	// Este metodo me sirve para validar si el participante ya selecciono algun dato
	// de viaje y obligara a lllenar todos en caso de que no
	// Si puede enviar null
	private boolean tieneDatosViaje(ParticipanteRegistroDTO dto) {
		return dto.tipoTransporte() != null
				|| dto.empresaTransporte() != null && !dto.empresaTransporte().isBlank()
				|| (dto.nroVuelo() != null && !dto.nroVuelo().isBlank())
				|| dto.fechaLlegada() != null
				|| dto.fechaIda() != null
				|| (dto.boletoUrl() != null && !dto.boletoUrl().isBlank())
				|| (dto.lugarLlegada() != null && !dto.lugarLlegada().isBlank());
	}

}
