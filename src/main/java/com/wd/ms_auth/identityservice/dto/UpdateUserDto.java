package com.wd.ms_auth.identityservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDto {
    /** 
     * El id del usuario 
    */
    @NotNull(message = "El id es obligatorio")
    private Long id;
    /** 
     * El nombre del usuario 
    */
    @NotBlank(message = "El nombre es obligatorio")
    private String firstName;

    /** 
     * El apellido del usuario 
    */
    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    /** 
     * El documento del usuario 
    */
    @NotBlank(message = "El documento es obligatorio")
    private String documentNumber;

    /** 
     * El correo del usuario 
    */
    @Email(message = "Correo inválido")
    @NotBlank(message = "El correo es obligatorio")
    private String email;

    /** 
     * El estado del usuario 
     */
    @NotNull(message = "El estado es obligatorio")
    private Boolean active;
}
