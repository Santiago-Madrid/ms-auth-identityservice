package com.wd.ms_auth.identityservice.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wd.ms_auth.identityservice.dto.JwtDto;
import com.wd.ms_auth.identityservice.dto.LoginRequestDto;
import com.wd.ms_auth.identityservice.dto.RegisterRequestDto;
import com.wd.ms_auth.identityservice.dto.RegisterResponseDto;
import com.world_dance.wd_lib_common.dto.HttpGlobalResponse;
import com.world_dance.wd_lib_common.entity.User;
import com.world_dance.wd_lib_common.exception.BadRequestException;
import com.world_dance.wd_lib_common.repository.UserRepository;
import com.world_dance.wd_lib_common.repository.PasswordRecoveryTokenRepository;
import com.world_dance.wd_lib_common.entity.PasswordRecoveryToken;
import com.world_dance.wd_lib_common.dto.PasswordRecoveryRequestDto;
import com.world_dance.wd_lib_common.dto.PasswordResetRequestDto;
import java.time.LocalDateTime;
import java.util.Random;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final EmailService emailService;

    private final PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;

    /**
     * Registra usuario en el sistema
     * @param registerRequestDto
     * @return RegisterResponseDto
     */
    public RegisterResponseDto register(RegisterRequestDto request) {
        
        RegisterResponseDto response = new RegisterResponseDto();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("El correo ya se encuentra registrado");
        }

        if(userRepository.existsByDocumentNumber(request.getDocumentNumber())) {
            throw new RuntimeException("El numero de documento ya se encuentra registrado");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDocumentNumber(request.getDocumentNumber());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));


        User savedUser = userRepository.save(user);


        response.setId(savedUser.getId());
        response.setDocumentNumber(savedUser.getDocumentNumber());
        response.setFirstName(savedUser.getFirstName());
        response.setLastName(savedUser.getLastName());
        response.setEmail(savedUser.getEmail());
        response.setActive(savedUser.getActive());
        response.setMessage("Usuario registrado correctamente");
        return response;
    }
    
    /**
     * Inicio de sesión de usuario
     * 
     * @param request
     * @return HttpGlobalResponse<JwtDto>
     */
    public HttpGlobalResponse<JwtDto> login(LoginRequestDto request) {
        HttpGlobalResponse<JwtDto> response = new HttpGlobalResponse<>();
        Optional<User> userFound = userRepository.findByEmail(request.getEmail());


        if (userFound.isEmpty()) {
            response.setMessage("Este usuario no se encuentra registrado");
            return response;
        }

        User user = userFound.get();


        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            response.setMessage("Correo o contraseña son incorrectos");
            return response;
        }

        JwtDto jwtDTO = new JwtDto();
        String jwt = jwtService.generateToken(user.getId(), user.getEmail());
        jwtDTO.setJwt(jwt);

        com.world_dance.wd_lib_common.dto.UserResponseDto userDto = new com.world_dance.wd_lib_common.dto.UserResponseDto();
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setDocumentNumber(user.getDocumentNumber());
        userDto.setEmail(user.getEmail());
        userDto.setActive(user.getActive());
        jwtDTO.setUser(userDto);

        response.setMessage("Inicio de sesión exitoso");
        response.setData(jwtDTO);
        return response;
    }

    /**
     * Refresca el token de seguridad, generando uno nuevo con la misma información pero con nueva expiración
     * @param token
     * @return response con nuevo token
     * @throws Exception
     */
    public JwtDto refreshToken(String token) throws Exception {
        JwtDto response = new JwtDto();
        String jwt = jwtService.refreshToken(token);
        response.setJwt(jwt);
        return response;
    }

    /**
     * Genera un código de recuperación de contraseña y lo envía por correo
     * @param request
     */
    public HttpGlobalResponse<Void> recoverPassword(PasswordRecoveryRequestDto request) {
        HttpGlobalResponse<Void> response = new HttpGlobalResponse<>();
        Optional<User> userFound = userRepository.findByEmail(request.getEmail());

        if (userFound.isEmpty()) {
            response.setMessage("Si el correo existe en nuestro sistema, recibirá un código de recuperación");
            return response;
        }

        User user = userFound.get();

        // Generar código numérico de 6 dígitos
        Random random = new Random();
        int codeInt = 100000 + random.nextInt(900000);
        String code = String.valueOf(codeInt);

        // Invalidar códigos anteriores
        Optional<PasswordRecoveryToken> previousToken = passwordRecoveryTokenRepository.findTopByUserAndUsedFalseOrderByExpiryDateDesc(user);
        if (previousToken.isPresent()) {
            PasswordRecoveryToken prev = previousToken.get();
            prev.setUsed(true);
            passwordRecoveryTokenRepository.save(prev);
        }

        PasswordRecoveryToken token = new PasswordRecoveryToken();
        token.setCode(code);
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        token.setUsed(false);

        passwordRecoveryTokenRepository.save(token);

        emailService.sendPasswordRecoveryEmail(user.getEmail(), code);

        response.setMessage("Si el correo existe en nuestro sistema, recibirá un código de recuperación");
        return response;
    }

    /**
     * Resetea la contraseña utilizando el código de recuperación
     * @param request
     */
    public HttpGlobalResponse<Void> resetPassword(PasswordResetRequestDto request) {
        HttpGlobalResponse<Void> response = new HttpGlobalResponse<>();
        Optional<User> userFound = userRepository.findByEmail(request.getEmail());

        if (userFound.isEmpty()) {
            throw new BadRequestException("El código es inválido o ha expirado");
        }

        User user = userFound.get();

        Optional<PasswordRecoveryToken> tokenFound = passwordRecoveryTokenRepository.findByCodeAndUserAndUsedFalse(request.getCode(), user);

        if (tokenFound.isEmpty() || tokenFound.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("El código es inválido o ha expirado");
        }

        PasswordRecoveryToken token = tokenFound.get();
        
        // Actualizar contraseña
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Marcar token como usado
        token.setUsed(true);
        passwordRecoveryTokenRepository.save(token);

        response.setMessage("Contraseña actualizada correctamente");
        return response;
    }
}
