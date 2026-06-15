package com.goodNews.genesis.modulos.usuarios.services;

import com.goodNews.genesis.modulos.usuarios.dtos.UserRequest;
import com.goodNews.genesis.modulos.auth.dtos.UserResponse;
import com.goodNews.genesis.modulos.usuarios.dtos.UserListResponse;
import com.goodNews.genesis.modulos.usuarios.entities.UsersEntity;
import com.goodNews.genesis.modulos.usuarios.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse crearUsuario(UserRequest dto) {
        UsersEntity newUser = new UsersEntity();

        newUser.setName(dto.nombre());
        newUser.setEmail(dto.email());
        newUser.setPassword(passwordEncoder.encode(dto.password()));
        newUser.setRol(dto.rol());
        newUser.setPais(dto.pais());
        newUser.setEstado(dto.estado());

        UsersEntity savedUser = userRepository.save(newUser);
        return new UserResponse(
                savedUser.getUserId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRol(),
                savedUser.getEstado(),
                savedUser.getPais()
        );
    }

    public List<UserListResponse> listarUsuarios() {
        List<UsersEntity> usuarios = userRepository.findAll();
        return usuarios.stream()
                .map(user -> new UserListResponse(
                        user.getName(),
                        user.getEmail(),
                        null, // No enviamos el hash de la contraseña al frontend
                        user.getRol(),
                        user.getPais(),
                        user.getEstado()
                ))
                .toList();
    }
}
