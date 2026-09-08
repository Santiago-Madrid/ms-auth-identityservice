package com.wd.ms_auth.identityservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wd.ms_auth.identityservice.dto.JwtDto;
import com.wd.ms_auth.identityservice.dto.LoginRequestDto;
import com.wd.ms_auth.identityservice.dto.RegisterRequestDto;
import com.wd.ms_auth.identityservice.dto.RegisterResponseDto;
import com.wd.ms_auth.identityservice.service.AuthService;
import com.world_dance.wd_lib_common.dto.HttpGlobalResponse;
import com.world_dance.wd_lib_common.dto.PasswordRecoveryRequestDto;
import com.world_dance.wd_lib_common.dto.PasswordResetRequestDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    
    private final AuthService authService;

    /**
     * Registro de usuario
     * @param request
     * @return RegisterResponseDto
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        
        RegisterResponseDto response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

     /**
     * Inicio de sesion del usuario
     * 
     * @param request
     * @return HttpGlobalResponse<JwtDTO>
     */
    @PostMapping("/login")
    public ResponseEntity<HttpGlobalResponse<JwtDto>> login(@RequestBody LoginRequestDto request) {
        try {
            HttpGlobalResponse<JwtDto> response = authService.login(request);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    /**
     * organizamos el token con su espacio y el prefijo Bearer
     * @param request
     * @return 
     */
    @GetMapping("/refresh")
    public ResponseEntity<JwtDto> refreshToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String token = authHeader.replaceFirst("Bearer ", "");

        JwtDto response = new JwtDto();

        try {
            response = authService.refreshToken(token);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @PostMapping("/recover-password")
    public ResponseEntity<HttpGlobalResponse<Void>> recoverPassword(@Valid @RequestBody PasswordRecoveryRequestDto request) {
        HttpGlobalResponse<Void> response = authService.recoverPassword(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<HttpGlobalResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetRequestDto request) {
        try {
            HttpGlobalResponse<Void> response = authService.resetPassword(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            HttpGlobalResponse<Void> errorResponse = new HttpGlobalResponse<>();
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
    

