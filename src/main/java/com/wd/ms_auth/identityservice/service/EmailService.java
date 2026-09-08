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

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public void sendPasswordRecoveryEmail(String toEmail, String code) {
        try {
            Context context = new Context();
            context.setVariable("code", code);

            String htmlContent = templateEngine.process("password-recovery", context);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Código de Recuperación de Contraseña - WORLD DANCE");
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("Recovery email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send email to {}", toEmail, e);
            throw new RuntimeException("Error al enviar el correo de recuperación", e);
        }
    }
}
