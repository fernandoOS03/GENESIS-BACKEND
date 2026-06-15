package com.goodNews.genesis.core.security.jwt;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {

    @Value("${jwt.secretString.key}")
    private String CLAVE_SECRETA;
    private SecretKey clave;

    @PostConstruct
    public void init() {
        this.clave = Keys.hmacShaKeyFor(CLAVE_SECRETA.getBytes(StandardCharsets.UTF_8));
    }

    public String generarToken(String correo) {
        return generarToken(correo, new HashMap<>());
    }

    public String generarToken(String correo, Map<String, Object> datosExtra) {
        return Jwts.builder()
                .setClaims(datosExtra)
                .setSubject(correo)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 horas
                .signWith(clave, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims obtenerDatos(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(clave).build()
                .parseClaimsJws(token).getBody();
    }

    public boolean validarToken(String token, String correo) {
        try {
            return obtenerDatos(token).getSubject().equals(correo);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

