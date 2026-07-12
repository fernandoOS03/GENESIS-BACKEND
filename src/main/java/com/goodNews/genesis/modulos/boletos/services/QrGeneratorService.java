package com.goodNews.genesis.modulos.boletos.services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio utilitario para generar códigos QR usando la librería ZXing.
 * El contenido del QR es el UUID del participante para validación en el evento.
 */
@Slf4j
@Service
public class QrGeneratorService {

    private static final int QR_SIZE_PX = 150;
    private static final String QR_FORMAT = "PNG";

    /**
     * Genera un código QR como array de bytes PNG.
     *
     * @param contenido El texto a codificar en el QR (UUID del participante)
     * @return byte[] con la imagen PNG del QR
     * @throws RuntimeException si falla la generación
     */
    public byte[] generarQr(String contenido) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1); // margen mínimo para aprovechar espacio en el boleto

            QRCodeWriter qrWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrWriter.encode(contenido, BarcodeFormat.QR_CODE, QR_SIZE_PX, QR_SIZE_PX, hints);

            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(qrImage, QR_FORMAT, outputStream);

            return outputStream.toByteArray();

        } catch (WriterException | IOException e) {
            log.error("Error al generar QR para contenido '{}': {}", contenido, e.getMessage(), e);
            throw new RuntimeException("No se pudo generar el código QR", e);
        }
    }
}
