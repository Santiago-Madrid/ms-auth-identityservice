package com.wd.ms_auth.identityservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequestDto {
  
    /**
     * Correo del usuario
    */
   @Email(message = "El correo no tiene un formato válido")
   @NotNull(message = "El correo es obligatorio")
   private String email;
   
   /**
    * Contraseña del usuario
   */
  @Size(min = 8, message = "La contraseña debe tener minimo 8 caracteres")
  @NotNull(message = "La contraseña es obligatoria")
  private String password;
  
}
