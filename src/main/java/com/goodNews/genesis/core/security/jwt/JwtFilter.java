package com.goodNews.genesis.core.security.jwt;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.goodNews.genesis.core.security.CustomUserDetails;
import com.goodNews.genesis.shared.enums.UsersEnum;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String encabezadoAutorizacion = request.getHeader("Authorization");

        if (encabezadoAutorizacion != null && encabezadoAutorizacion.startsWith("Bearer ")) {
            String token = encabezadoAutorizacion.substring(7);
            try {
                Claims datos = jwtUtil.obtenerDatos(token);
                String correo = datos.getSubject();
                String rolTexto = datos.get("rol", String.class);
                String pais = datos.get("pais", String.class);
                String usuarioIdTexto = datos.get("userId", String.class);

                if (correo != null && rolTexto != null) {
                    UsersEnum rol = UsersEnum.valueOf(rolTexto);
                    UUID usuarioId = usuarioIdTexto != null ? UUID.fromString(usuarioIdTexto) : null;

                    CustomUserDetails detallesUsuario = new CustomUserDetails(usuarioId, correo, rol, pais);

                    UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken(
                            detallesUsuario, null, detallesUsuario.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            } catch (Exception e) {
                // Si falla la validación del token, no se establece la autenticación.
                // Se neiega acceso automáticamente a las rutas protegidas.
            }
        }
        filterChain.doFilter(request, response);
    }
}


