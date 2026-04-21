package com.fml.fluxa.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100)
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, max = 100)
        String lastName,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        String email,

        // Mínimo 8 caracteres, al menos 1 mayúscula, 1 número, 1 carácter especial
        @NotBlank(message = "La contraseña es obligatoria")
        @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un carácter especial"
        )
        String password
) {}
