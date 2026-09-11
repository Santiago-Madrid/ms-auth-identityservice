package com.wd.ms_auth.identityservice.dto;

import com.world_dance.wd_lib_common.dto.UserResponseDto;

import lombok.Data;

@Data
public class JwtDto {
    /**
     * JWT del usuario logueado
     */
    private String jwt;

    private UserResponseDto user;
}
