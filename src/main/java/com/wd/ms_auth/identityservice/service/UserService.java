package com.wd.ms_auth.identityservice.service;

import org.springframework.stereotype.Service;

import com.wd.ms_auth.identityservice.dto.UpdateUserDto;
import com.world_dance.wd_lib_common.dto.UserResponseDto;
import com.world_dance.wd_lib_common.entity.User;
import com.world_dance.wd_lib_common.exception.BadRequestException;
import com.world_dance.wd_lib_common.repository.UserRepository;

import lombok.RequiredArgsConstructor; 

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Actualiza un usuario existente en la base de datos. Primero verifica que el
     * id del usuario no sea nulo, luego busca el usuario por su id. Si el usuario
     * no existe, lanza una excepción BadRequestException. Si el usuario existe,
     * actualiza sus campos con los valores del UpdateUserDto y guarda el usuario
     * actualizado en la base de datos. Finalmente, devuelve un nuevo UpdateUserDto
     * con los datos actualizados del usuario.
     * 
     * @param dto
     * @return usuario actualizado
     */
    public UpdateUserDto updateUser(UpdateUserDto dto) {

        if (dto.getId() == null) {
            throw new BadRequestException("El id del usuario es obligatorio");
        }

        User user = userRepository.findById(dto.getId())
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setDocumentNumber(dto.getDocumentNumber());
        user.setEmail(dto.getEmail());
        user.setActive(dto.getActive());

        User updatedUser = userRepository.save(user);

        return new UpdateUserDto(
                updatedUser.getId(),
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getDocumentNumber(),
                updatedUser.getEmail(),
                updatedUser.getActive());
    }

    /**
     * Este método obtiene un usuario por su número de documento. Busca el usuario
     * en la base de datos utilizando el número de documento proporcionado. Si el
     * usuario existe, crea un UserResponseDto con los datos del usuario y lo
     * devuelve. Si el usuario no existe, lanza una excepción BadRequestException
     * indicando que el usuario no fue encontrado.
     * 
     * @param documentNumber
     * @return usuario encontrado
     */
    public UserResponseDto getUser(String documentNumber) {

        if (documentNumber == null || documentNumber.isBlank()) {
            throw new BadRequestException("El numero de documento es obligatorio");
        }

        User user = userRepository.findByDocumentNumber(documentNumber)
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));

        UserResponseDto response = new UserResponseDto();

        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setDocumentNumber(user.getDocumentNumber());
        response.setEmail(user.getEmail());
        response.setActive(user.getActive());

        return response;

    }


}