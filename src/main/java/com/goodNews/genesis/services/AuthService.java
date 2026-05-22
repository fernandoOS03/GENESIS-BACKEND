package com.goodNews.genesis.services;

import com.goodNews.genesis.dtos.user.LoginAdminRequestDTO;
import com.goodNews.genesis.dtos.user.LoginAdminResponseDTO;
import com.goodNews.genesis.dtos.user.UserResponse;
import com.goodNews.genesis.entities.UsersEntity;
import com.goodNews.genesis.exceptions.BadRequestException;
import com.goodNews.genesis.repository.UserRespository;
import com.goodNews.genesis.shared.services.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRespository userRespository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginAdminResponseDTO login(LoginAdminRequestDTO request) {
        UsersEntity usuario = userRespository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new BadRequestException("Contraseña Incorrecta");
        }

        //Se prepara el formato de envio al frontend
        UserResponse userData = new UserResponse(
                usuario.getUserId(),
                usuario.getName(),
                usuario.getEmail(),
                usuario.getRol()
        );

        //Generamos el token
        //Generamos un mapa para los valors extra que iran dentro del payload del JWT
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("rol", usuario.getRol().name());
        extraClaims.put("userId", usuario.getUserId());

        String token = jwtService.generateToken(usuario.getEmail(), extraClaims);

        return new LoginAdminResponseDTO(token, userData);


    }
}
