package com.wd.ms_auth.identityservice.dto;

import lombok.Data;

@Data
public class JwtDto {
    /**
     * JWT del usuario logueado
     */
    private String jwt;
}
