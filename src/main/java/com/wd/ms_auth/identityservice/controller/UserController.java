package com.wd.ms_auth.identityservice.controller;


import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wd.ms_auth.identityservice.dto.UpdateUserDto;
import com.wd.ms_auth.identityservice.security.RequireRole;
import com.wd.ms_auth.identityservice.service.UserService;
import com.world_dance.wd_lib_common.dto.ErrorResponseDto;
import com.world_dance.wd_lib_common.dto.UserResponseDto;
import com.world_dance.wd_lib_common.enums.Role;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    
    private final UserService userService;
    /**
     * controllador para actualizar un usuario, recibe un UpdateUserDto con los datos actualizados y devuelve el UpdateUserDto actualizado
     * @param userDto
     * @return usuario actualizado
    */
    @PutMapping("/update")
    public ResponseEntity<UpdateUserDto> updateUser(@RequestBody UpdateUserDto userDto) {
        
        UpdateUserDto updatedUser = userService.updateUser(userDto);
        
        return ResponseEntity.ok(updatedUser);
    }
    
    /**
     * controlador para obtener un usuario por su numero de documento, recibe el numero de documento como parametro y devuelve un UserResponseDto con los datos del usuario
     * @param documentNumber
     * @return usuario encontrado
    */
    
    @RequireRole({Role.ADMIN, Role.ORGANIZER, Role.STAFF})
    @GetMapping("/document/{documentNumber}")
    public ResponseEntity<?> getUserByDocumentNumber(@PathVariable String documentNumber) {
        try {
            UserResponseDto response = userService.getUser(documentNumber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.badRequest().body(error);
        }
    } 
    

}