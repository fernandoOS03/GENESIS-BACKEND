package com.goodNews.genesis.shared.services;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.RawMessage;
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest;

/**
 * Servicio de email extendido que soporta adjuntos binarios (PDF).
 * Usa SES SendRawEmail (MIME multipart) para poder incluir el boleto PDF.
 * No reemplaza al EmailService original; ambos pueden coexistir.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailWithAttachmentService {

    private final SesClient sesClient;

    @Value("${aws.ses.sender-email}")
    private String remitente;

    /**
     * Envía un correo HTML con un archivo PDF adjunto.
     *
     * @param destino          Dirección de correo del destinatario
     * @param asunto           Asunto del correo
     * @param bodyHtml         Cuerpo en formato HTML
     * @param pdfBytes         Contenido binario del PDF
     * @param nombreArchivoPdf Nombre del archivo adjunto (ej: "boleto-JUAN-PEREZ.pdf")
     */
    @Async
    public void sendEmailWithPdf(String destino, String asunto, String bodyHtml,
            byte[] pdfBytes, String nombreArchivoPdf) {
        try {
            // Sesión MIME sin servidor SMTP (solo usamos el builder para construir el mensaje)
            Session session = Session.getInstance(new Properties());
            MimeMessage mimeMessage = new MimeMessage(session);

            mimeMessage.setFrom(new InternetAddress(remitente));
            mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(destino));
            mimeMessage.setSubject(asunto, "UTF-8");

            // --- Parte 1: Cuerpo HTML ---
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(bodyHtml, "text/html; charset=UTF-8");

            // --- Parte 2: PDF adjunto ---
            MimeBodyPart pdfPart = new MimeBodyPart();
            pdfPart.setFileName(nombreArchivoPdf);
            pdfPart.setContent(pdfBytes, "application/pdf");
            pdfPart.setHeader("Content-Transfer-Encoding", "base64");

            // --- Multipart que agrupa ambas partes ---
            MimeMultipart multipart = new MimeMultipart("mixed");
            multipart.addBodyPart(htmlPart);
            multipart.addBodyPart(pdfPart);
            mimeMessage.setContent(multipart);

            // Serializar el MimeMessage a bytes para SES Raw
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mimeMessage.writeTo(outputStream);
            byte[] rawMessageBytes = outputStream.toByteArray();

            RawMessage rawMessage = RawMessage.builder()
                    .data(SdkBytes.fromByteArray(rawMessageBytes))
                    .build();

            SendRawEmailRequest rawRequest = SendRawEmailRequest.builder()
                    .rawMessage(rawMessage)
                    .build();

            sesClient.sendRawEmail(rawRequest);
            log.info("Boleto PDF enviado exitosamente a: {}", destino);

        } catch (MessagingException | java.io.IOException e) {
            log.error("Error al construir/enviar el correo con PDF adjunto para {}: {}", destino, e.getMessage(), e);
            throw new RuntimeException("No se pudo enviar el boleto PDF a " + destino, e);
        }
    }
}
