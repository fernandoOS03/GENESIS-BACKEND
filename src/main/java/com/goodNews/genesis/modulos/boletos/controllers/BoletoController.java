package com.goodNews.genesis.modulos.boletos.controllers;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goodNews.genesis.modulos.boletos.services.BoletoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller REST para la generación y envío de boletos PDF — World Camp 2027.
 *
 * <p>Todos los endpoints requieren autenticación de administrador
 * (gestionado por el SecurityConfig del proyecto).</p>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *   <li>{@code POST /api/v1/boletos/enviar/{id}} — Genera el PDF y lo envía al correo
 *       del participante. El participante debe tener estado {@code CONFIRMADO}.</li>
 *   <li>{@code GET  /api/v1/boletos/preview/{id}} — Devuelve el PDF directamente
 *       para descarga o previsualización en el navegador (sin enviar correo).</li>
 * </ul>
 *
 * <p><strong>Nota:</strong> El envío masivo a todos los participantes CONFIRMADOS
 * está planificado para implementarse con <em>Spring Batch</em> en una fase futura.
 * Esta versión solo soporta envío individual boleto a boleto.</p>
 */
@Slf4j
@RestController
@RequestMapping("api/v1/boletos")
@RequiredArgsConstructor
public class BoletoController {

    private final BoletoService boletoService;

    // ===========================================================
    // 1. Enviar boleto individual al correo del participante
    // ===========================================================

    /**
     * Genera el boleto PDF del participante indicado y lo envía a su dirección de correo.
     *
     * <p>Requisito: el participante debe tener {@code estadoGeneral = CONFIRMADO}.
     * Si no está CONFIRMADO, retorna HTTP 400 con un mensaje descriptivo.</p>
     *
     * @param id UUID del participante
     * @return 200 OK con mensaje de confirmación, o error descriptivo
     */
    @PostMapping("/enviar/{id}")
    public ResponseEntity<Map<String, String>> enviarBoleto(@PathVariable UUID id) {
        log.info("Solicitud de envío de boleto para participante: {}", id);

        try {
            boletoService.enviarBoletoIndividual(id);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Boleto generado y enviado exitosamente al correo del participante.",
                    "participanteId", id.toString()
            ));
        } catch (IllegalStateException ex) {
            // El participante no está en estado CONFIRMADO
            log.warn("Intento de envío de boleto para participante no confirmado {}: {}", id, ex.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", ex.getMessage(),
                            "participanteId", id.toString()
                    ));
        }
    }

    // ===========================================================
    // 2. Preview / descarga directa del PDF (sin enviar correo)
    // ===========================================================

    /**
     * Genera el boleto PDF y lo devuelve directamente como respuesta HTTP
     * con {@code Content-Type: application/pdf}.
     *
     * <p>Útil para revisar el diseño del boleto antes del envío masivo.
     * No requiere que el participante esté CONFIRMADO.</p>
     *
     * @param id UUID del participante
     * @return PDF binario inline para previsualización
     */
    @GetMapping("/preview/{id}")
    public ResponseEntity<byte[]> previewBoleto(@PathVariable UUID id) {
        log.info("Solicitud de preview de boleto para participante: {}", id);

        byte[] pdfBytes = boletoService.generarPdfParticipante(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename("boleto-worldcamp2027-preview-" + id + ".pdf")
                        .build()
        );
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
