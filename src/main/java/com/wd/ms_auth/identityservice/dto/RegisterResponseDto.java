package com.wd.ms_auth.identityservice.dto;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonPropertyOrder({
    "id", 
    "firstName", 
    "lastName", 
    "documentNumber", 
    "email", 
    "active", 
    "message"
})
public class RegisterResponseDto {

    /**
     * Este  es el id del usuario
     */
    private Long id;
    /**
     * Este es el primer nombre del usuario
     */
    private String firstName;
    /**
     * Este es el apellido del usuario
     */
    private String lastName;
    /**
     * Este es el numero de documento del usuario
     */
    private String documentNumber;
    /**
     * Este es el correo electrónico del usuario
     */
    private String email;
    /**
     * Este es el estado del usuario
     */
    private Boolean active;
    /**
     * Este es el mensaje de respuesta
     */
    private String message;
}
