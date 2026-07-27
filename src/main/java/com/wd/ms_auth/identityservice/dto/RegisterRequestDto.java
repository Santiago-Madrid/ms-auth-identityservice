package com.wd.ms_auth.identityservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDto {
    /**
     * Este es el primer nombre del usuario
     */
    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;
    /**
     * Este es el apellido del usuario
     */
    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;
    /**
     * Este es el numero de documento del usuario
     */
    @NotBlank(message = "El numero de documento es obligatorio")
    @Size(min = 6, max = 20, message = "El numero de docuemento debe tener entre 6 y 20 caracteres")
    private String documentNumber;
    /**
     * Este es el correo electrónico del usuario
     */
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato del correo no es valido")
    private String email;
    /**
     * Este es la contraseña del usuario
     */ 
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener minimo 8 caracteres")
    private String password;
}
