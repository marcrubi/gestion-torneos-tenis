// src/main/java/com/ISII/gestion_torneos_tenis/dto/PasswordResetRequest.java

package com.ISII.gestion_torneos_tenis.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class PasswordResetRequest {

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "El correo electrónico debe ser válido.")
    private String correoElectronico;

    // Getters y Setters

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }
}
