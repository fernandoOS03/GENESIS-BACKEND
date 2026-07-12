package com.goodNews.genesis.modulos.boletos.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.goodNews.genesis.core.exceptions.ResourceNotFoundException;
import com.goodNews.genesis.modulos.boletos.dtos.BoletoParticipanteDTO;
import com.goodNews.genesis.modulos.participantes.entities.ParticipantsEntity;
import com.goodNews.genesis.modulos.participantes.repositories.ParticipantRepository;
import com.goodNews.genesis.shared.enums.EstadoGeneralEnum;
import com.goodNews.genesis.shared.services.EmailWithAttachmentService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoletoService {

    private final ParticipantRepository participantRepository;
    private final QrGeneratorService qrGeneratorService;
    private final EmailWithAttachmentService emailWithAttachmentService;

    /**
     * TemplateEngine dedicado para el boleto PDF.
     * Usa modo XML (no HTML5) porque OpenHTMLToPDF requiere XHTML bien formado.
     * Se usa SpringTemplateEngine porque Spring Boot excluye OGNL a favor de SpEL.
     */
    private static final SpringTemplateEngine BOLETO_TEMPLATE_ENGINE = crearBoletoTemplateEngine();

    private static SpringTemplateEngine crearBoletoTemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.XML);   // ← clave: XHTML estricto
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);                  // false para desarrollo; true en producción
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    // =========================================================
    // Configurables desde application.properties
    // =========================================================
    @Value("${boleto.default.nroBus:Por confirmar}")
    private String nroBusPorDefecto;

    @Value("${boleto.default.hotel:Por confirmar}")
    private String hotelPorDefecto;

    @Value("${boleto.default.nroHabitacion:Por confirmar}")
    private String nroHabitacionPorDefecto;

    @Value("${boleto.default.maestro:Por confirmar}")
    private String maestroPorDefecto;

    @Value("${boleto.default.grupo:Por confirmar}")
    private String grupoPorDefecto;

    // =========================================================
    // Constantes del evento
    // =========================================================
    private static final String TITULO_EVENTO   = "IYF PERU | INTERNATIONAL YOUTH FELLOWSHIP";
    private static final String NOMBRE_EVENTO   = "WORLD CAMP 2027";
    private static final String FECHAS_EVENTO   = "08/02/2026 - 12/02/2026";
    private static final String LUGAR_EVENTO    = "LIMA, PERÚ";
    private static final String AVISO_SEGURIDAD =
            "*Recuerda: Tu código QR es lo más\nimportante de tu boleto.\nCuídalo, no lo publiques ni\nlo compartas con nadie.";
    private static final String PIE_LEGAL =
            "Conserve su boleto. Este documento es su identificación en el evento World Camp 2027.";
    private static final String TERMINOS_INTRO =
            "¡Hola, felicitaciones! Tu inscripción ha sido confirmada. " +
            "Como todo en esta vida tiene sus reglas, aquí te mostramos las de este boleto " +
            "(Léelas todas y no te aburras):";
    private static final String TERMINOS_TEXTO =
            "• Joinnus, o sea nosotros, vende o emite esta entrada a nombre del organizador del evento.\n" +
            "• No somos responsables de lo que suceda antes, durante o después del evento al que vayas.\n" +
            "• En caso de cancelación o reprogramación del evento, la organización IYF Perú es el único responsable de la devolución del dinero de las entradas.\n" +
            "• Los organizadores siempre tienen la última palabra en cuanto al ingreso al evento. O sea, se reservan el derecho de admisión y permanencia.\n" +
            "• En todo evento hay un control de seguridad en el que se te pedirá que no ingreses objetos que puedan considerarse peligrosos: alimentos, bebidas, cámaras fotográficas, dispositivos de filmación.\n" +
            "• Prohibido ingresar dispositivos pirotécnicos, sustancias inflamables o corrosivas.\n" +
            "• Prohibido el uso de pintura facial, prendas o accesorios que dificulten la identificación.\n" +
            "• Prohibido introducir, vender o encontrarse bajo los efectos de bebidas alcohólicas, estupefacientes, psicotrópicos o sustancias análogas.";

    // =========================================================
    // Rutas de recursos (src/main/resources/)
    // Imágenes en static/imagenes/ según estructura real del proyecto
    // =========================================================
    private static final String PATH_LOGO        = "static/imagenes/logo-iyg.png";
    private static final String PATH_FONDO_WC    = "static/imagenes/fondo-wc.png";

    // (Paleta de colores gestionada ahora en el template HTML/CSS)

    // =========================================================
    // API Pública
    // =========================================================

    /**
     * Genera el PDF del boleto de un participante CONFIRMADO y lo envía a su correo.
     *
     * @param participanteId UUID del participante
     * @throws ResourceNotFoundException si el participante no existe
     * @throws IllegalStateException     si el participante no está CONFIRMADO
     */
    public void enviarBoletoIndividual(UUID participanteId) {
        ParticipantsEntity participante = participantRepository.findById(participanteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Participante no encontrado: " + participanteId));

        if (participante.getEstadoGeneral() != EstadoGeneralEnum.CONFIRMADO) {
            throw new IllegalStateException(
                    "El participante con documento '" + participante.getNroDocumento() +
                    "' no tiene estado CONFIRMADO. Estado actual: " + participante.getEstadoGeneral());
        }

        BoletoParticipanteDTO dto = mapearADto(participante);
        byte[] pdfBytes = construirPdf(dto);

        emailWithAttachmentService.sendEmailWithPdf(
                participante.getEmail(),
                "Tu boleto — " + NOMBRE_EVENTO,
                construirCuerpoCorreo(dto),
                pdfBytes,
                dto.nombreArchivoPdf()
        );

        log.info("Boleto enviado exitosamente a: {} ({})", participante.getEmail(), participante.getNroDocumento());
    }

    /**
     * Genera el PDF del boleto sin enviarlo por correo.
     * Útil para preview/descarga directa desde el panel admin.
     *
     * @param participanteId UUID del participante
     * @return PDF como byte[]
     */
    public byte[] generarPdfParticipante(UUID participanteId) {
        ParticipantsEntity participante = participantRepository.findById(participanteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Participante no encontrado: " + participanteId));

        return construirPdf(mapearADto(participante));
    }

    // =========================================================
    // Mapeo entidad → DTO
    // =========================================================

    private BoletoParticipanteDTO mapearADto(ParticipantsEntity entity) {
        // Usar datos reales del grupo si el participante ya fue asignado;
        // de lo contrario, usar los valores por defecto de application.properties.
        String grupoNombre = grupoPorDefecto;
        String maestroNombre = maestroPorDefecto;

        if (entity.getGrupo() != null) {
            grupoNombre = entity.getGrupo().getNombre();
            if (entity.getGrupo().getMaestro() != null) {
                maestroNombre = entity.getGrupo().getMaestro().getNombres()
                        + " " + entity.getGrupo().getMaestro().getApellidos();
            }
        }

        return new BoletoParticipanteDTO(
                entity,
                nroBusPorDefecto,
                hotelPorDefecto,
                nroHabitacionPorDefecto,
                maestroNombre,
                grupoNombre
        );
    }

    // =========================================================
    // Construcción del PDF — flujo principal (OpenHTMLToPDF)
    // =========================================================

    private byte[] construirPdf(BoletoParticipanteDTO dto) {
        try {
            byte[] qrBytes     = qrGeneratorService.generarQr(dto.id().toString());
            String qrBase64    = Base64.getEncoder().encodeToString(qrBytes);
            String fondoBase64 = cargarBase64(PATH_FONDO_WC);

            String html = renderizarHtml(dto, qrBase64, fondoBase64);
            return htmlAPdf(html);

        } catch (Exception e) {
            log.error("Error generando PDF para {}: {}", dto.nroDocumento(), e.getMessage(), e);
            throw new RuntimeException("Error al generar el PDF del boleto", e);
        }
    }

    // =========================================================
    // Helpers — HTML → PDF (OpenHTMLToPDF)
    // =========================================================

    /**
     * Puebla la plantilla Thymeleaf boletos/boleto.html con los datos del participante
     * y devuelve el HTML renderizado como String.
     */
    private String renderizarHtml(BoletoParticipanteDTO dto, String qrBase64, String fondoWcBase64) {
        Context ctx = new Context();
        ctx.setVariable("dto",           dto);
        ctx.setVariable("qrBase64",      qrBase64);
        ctx.setVariable("fondoWcBase64", fondoWcBase64);
        return BOLETO_TEMPLATE_ENGINE.process("boletos/boleto", ctx);
    }

    /**
     * Convierte un String HTML a bytes de PDF usando OpenHTMLToPDF + PDFBox.
     * El HTML puede contener imágenes en Base64 (data: URIs), por lo que
     * no requiere acceso a internet ni a disco en runtime.
     */
    private byte[] htmlAPdf(String html) throws Exception {
        // Remover BOM (Byte Order Mark) y espacios iniciales que rompen el parser XML estricto
        html = html.replace("\uFEFF", "").trim();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, null);
        builder.toStream(baos);
        builder.run();
        return baos.toByteArray();
    }

    /**
     * Carga un recurso del classpath y lo devuelve codificado en Base64.
     * Si el archivo no existe devuelve cadena vacía (sin lanzar excepción).
     *
     * @param path Ruta classpath, ej. "static/imagenes/fondo-wc.png"
     * @return Base64 del archivo, o cadena vacía si no existe
     */
    private String cargarBase64(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    return Base64.getEncoder().encodeToString(is.readAllBytes());
                }
            }
            log.warn("Recurso no encontrado en classpath: {}", path);
        } catch (IOException e) {
            log.warn("No se pudo cargar recurso '{}': {}", path, e.getMessage());
        }
        return "";
    }


    // =========================================================
    // Cuerpo HTML del correo — Paleta alineada con el frontend Genesis
    // Primario: #004E9A (azul IYF)
    // Secundario: #15A444 (verde logo)
    // Fondo header: #09090b (negro profundo del admin)
    // =========================================================

    private String construirCuerpoCorreo(BoletoParticipanteDTO dto) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Tu boleto — World Camp 2027</title>
                </head>
                
                <body style="
                    margin:0;
                    padding:0;
                    background:#f5f5f5;
                    font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Helvetica,Arial,sans-serif;
                ">
                
                <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                <tr>
                <td align="center" style="padding:48px 24px;">
                
                <table width="620" cellpadding="0" cellspacing="0" border="0" style="
                    background:#ffffff;
                ">
                
                    <!-- Logo -->
                    <tr>
                        <td align="center" style="padding:48px 48px 0;">
                            <p style="
                                margin:0;
                                font-size:12px;
                                font-weight:700;
                                color:#9ca3af;
                                letter-spacing:2px;
                                text-transform:uppercase;
                            ">
                                IYF Perú
                            </p>
                        </td>
                    </tr>
                
                    <!-- Saludo -->
                    <tr>
                        <td align="center" style="padding:48px 56px 0;">
                            <p style="
                                margin:0;
                                font-size:18px;
                                color:#6b7280;
                                font-weight:400;
                            ">
                                Hola, <strong style="color:#111827;">%s</strong>
                            </p>
                        </td>
                    </tr>
                
                    <!-- Título principal -->
                    <tr>
                        <td align="center" style="padding:24px 56px 0;">
                            <h1 style="
                                margin:0;
                                font-family:Georgia,'Times New Roman',serif;
                                font-size:56px;
                                line-height:1.15;
                                font-weight:500;
                                color:#111827;
                                letter-spacing:-1.5px;
                            ">
                                Tu boleto para<br>
                                World Camp 2027
                            </h1>
                        </td>
                    </tr>
                
                    <!-- Descripción -->
                    <tr>
                        <td align="center" style="padding:32px 80px 0;">
                            <p style="
                                margin:0;
                                font-size:18px;
                                line-height:1.9;
                                color:#6b7280;
                                font-weight:400;
                            ">
                                Tu registro ha sido confirmado.
                                Adjuntamos tu boleto oficial para el ingreso al evento.
                            </p>
                        </td>
                    </tr>
                
                    <!-- Tarjeta Paso a Paso -->
                    <tr>
                        <td style="padding:56px;">
                            <table width="100%%" cellpadding="0" cellspacing="0" border="0"
                            style="
                                background:#f7f7f7;
                                border-radius:24px;
                            ">
                                <tr>
                                    <td style="padding:48px;">
                                        <p style="
                                            margin:0;
                                            font-size:12px;
                                            text-transform:uppercase;
                                            letter-spacing:2px;
                                            color:#9ca3af;
                                            font-weight:600;
                                        ">
                                            Qué hacer
                                        </p>
                
                                        <!-- Paso 1 -->
                                        <p style="
                                            margin:24px 0 0;
                                            font-size:28px;
                                            color:#111827;
                                            font-weight:700;
                                        ">
                                            01
                                        </p>
                                        <p style="
                                            margin:1px 0 0;
                                            font-size:15px;
                                            color:#6b7280;
                                            line-height:1.6;
                                        ">
                                            Descarga tu boleto PDF
                                        </p>
                
                                        <!-- Paso 2 -->
                                        <p style="
                                            margin:10px 0 0;
                                            font-size:28px;
                                            color:#111827;
                                            font-weight:700;
                                        ">
                                            02
                                        </p>
                                        <p style="
                                            margin: 0 0;
                                            font-size:15px;
                                            color:#6b7280;
                                            line-height:1.6;
                                        ">
                                            Preséntalo digital o impreso
                                        </p>
                
                                        <!-- Paso 3 -->
                                        <p style="
                                            margin:10px 0 0;
                                            font-size:28px;
                                            color:#111827;
                                            font-weight:700;
                                        ">
                                            03
                                        </p>
                                        <p style="
                                            margin:0px 0 0;
                                            font-size:15px;
                                            color:#6b7280;
                                            line-height:1.6;
                                        ">
                                            Disfruta del evento
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                
                    <!-- Footer -->
                    <tr>
                        <td align="center" style="padding:32px 56px 56px;">
                            <p style="
                                margin:0;
                                font-size:14px;
                                color:#9ca3af;
                                letter-spacing:.3px;
                            ">
                                World Camp 2027
                            </p>
                
                            <p style="
                                margin:10px 0 0;
                                font-size:13px;
                                color:#c0c4cc;
                            ">
                                International Youth Fellowship
                            </p>
                        </td>
                    </tr>
                
                </table>
                
                </td>
                </tr>
                </table>
                
                </body>
                </html>
                """.formatted(
                NOMBRE_EVENTO,          // %s h1
                dto.nombreCompleto(),   // %s saludo
                FECHAS_EVENTO,          // %s fechas
                LUGAR_EVENTO,           // %s lugar
                dto.tipoDocumento(),    // %s label doc
                dto.nroDocumento(),     // %s nro doc
                dto.sede()              // %s sede
        );
    }
}
