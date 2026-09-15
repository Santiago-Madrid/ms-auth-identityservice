package com.wd.ms_auth.identityservice.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import java.io.UnsupportedEncodingException;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${SPRING_MAIL_USERNAME:}")
    private String fromEmail;

    public void sendPasswordRecoveryEmail(String toEmail, String code) {
        try {
            Context context = new Context();
            context.setVariable("code", code);

            String htmlContent = templateEngine.process("password-recovery", context);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "WORLD DANCE");
            helper.setTo(toEmail);
            helper.setSubject("Código de Recuperación de Contraseña - WORLD DANCE");
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("Recovery email sent successfully to: {}", toEmail);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send email to {}", toEmail, e);
            throw new RuntimeException("Error al enviar el correo de recuperación", e);
        }
    }
}
