package com.goodNews.genesis.core.security;

import com.goodNews.genesis.modulos.participantes.entities.ParticipantsEntity;
import com.goodNews.genesis.modulos.participantes.repositories.ParticipantRepository;
import com.goodNews.genesis.shared.enums.UsersEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("securityHelper")
@RequiredArgsConstructor
public class SecurityHelper {

    private final ParticipantRepository participantRepository;

    /**
     * Obtiene los detalles del usuario actualmente autenticado en el sistema.
     */
    public CustomUserDetails getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }
        return null;
    }

    /**
     * Determina si el usuario actual es un Super Admin.
     */
    public boolean isSuperAdmin() {
        CustomUserDetails usuarioActual = getCurrentUser();
        return usuarioActual != null && usuarioActual.getRol() == UsersEnum.ROLE_SUPER_ADMIN;
    }

    /**
     * Verifica si el usuario actual tiene permisos para acceder/operar sobre un determinado país.
     * - SUPER_ADMIN tiene acceso a cualquier país.
     * - COUNTRY_ADMIN y EDITOR solo tienen acceso si su país coincide.
     */
    public boolean canAccessCountry(String pais) {
        CustomUserDetails usuarioActual = getCurrentUser();
        if (usuarioActual == null) {
            return false;
        }
        if (usuarioActual.getRol() == UsersEnum.ROLE_SUPER_ADMIN) {
            return true;
        }
        return usuarioActual.getPais() != null && usuarioActual.getPais().equalsIgnoreCase(pais);
    }

    /**
     * Verifica si el usuario actual tiene permisos para editar o realizar operaciones sobre un participante.
     * - SUPER_ADMIN puede hacerlo siempre.
     * - COUNTRY_ADMIN y EDITOR solo si el participante es del mismo país que ellos.
     */
    public boolean canAccessParticipant(UUID participanteId) {
        CustomUserDetails usuarioActual = getCurrentUser();
        if (usuarioActual == null) {
            return false;
        }
        if (usuarioActual.getRol() == UsersEnum.ROLE_SUPER_ADMIN) {
            return true;
        }

        ParticipantsEntity participante = participantRepository.findById(participanteId).orElse(null);
        if (participante == null) {
            return false;
        }
        return canAccessCountry(participante.getPais());
    }

    /**
     * Verifica si el usuario actual puede editar información.
     * - SUPER_ADMIN puede hacerlo siempre.
     * - COUNTRY_ADMIN puede hacerlo en su país.
     * - EDITOR puede hacerlo en su país.
     */
    public boolean canEdit() {
        CustomUserDetails usuarioActual = getCurrentUser();
        if (usuarioActual == null) {
            return false;
        }
        // Todos los roles (SUPER_ADMIN, COUNTRY_ADMIN, EDITOR) pueden editar, pero condicionado por país.
        return true;
    }
}
