package com.goodNews.genesis.modulos.auth.services;

import com.goodNews.genesis.modulos.auth.dtos.LoginAdminRequestDTO;
import com.goodNews.genesis.modulos.auth.dtos.LoginAdminResponseDTO;
import com.goodNews.genesis.modulos.auth.dtos.UserResponse;
import com.goodNews.genesis.modulos.usuarios.entities.UsersEntity;
import com.goodNews.genesis.core.exceptions.BadRequestException;
import com.goodNews.genesis.modulos.usuarios.repositories.UserRepository;
import com.goodNews.genesis.core.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginAdminResponseDTO login(LoginAdminRequestDTO request) {
        UsersEntity usuario = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new BadRequestException("Contraseña Incorrecta");
        }

        // Se prepara el formato de envio al frontend
        UserResponse datosUsuario = new UserResponse(
                usuario.getUserId(),
                usuario.getName(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getEstado(),
                usuario.getPais()
        );

        // Generamos el token
        Map<String, Object> datosExtra = new HashMap<>();
        datosExtra.put("rol", usuario.getRol().name());
        datosExtra.put("userId", usuario.getUserId().toString());
        datosExtra.put("pais", usuario.getPais());

        String token = jwtUtil.generarToken(usuario.getEmail(), datosExtra);
        //System.out.println("Token: " + token);

        return new LoginAdminResponseDTO(token, datosUsuario);
    }
}
