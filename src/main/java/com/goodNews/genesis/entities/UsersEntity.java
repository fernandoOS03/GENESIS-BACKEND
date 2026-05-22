package com.goodNews.genesis.entities;

import com.goodNews.genesis.enums.UsersEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jdk.jfr.DataAmount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsersEntity {
    @Id
    @GeneratedValue
    private UUID userId;

    @Column(name = "nombre")
    private String name;

    @Column(length = 100, nullable = false)
    @Email
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)

    private UsersEnum rol;
}
