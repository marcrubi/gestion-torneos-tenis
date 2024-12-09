// src/main/java/com/ISII/gestion_torneos_tenis/service/EmailService.java

package com.ISII.gestion_torneos_tenis.service;

import jakarta.mail.internet.MimeMessage; // Reemplazado de javax a jakarta
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine; // Asegurado el uso de spring6
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Autowired
    public EmailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * Envía un correo de verificación de cuenta en formato HTML con reintentos.
     */
    @Retryable(
            value = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public boolean enviarCorreoVerificacionHtml(String to, String enlaceVerificacion) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Verificación de Cuenta - Gestión de Torneos de Tenis");
            helper.setFrom("gestiontorneostenis@gmail.com"); // Remitente

            // Preparar el contexto para Thymeleaf
            Context context = new Context();
            context.setVariable("enlaceVerificacion", enlaceVerificacion);

            // Procesar la plantilla Thymeleaf o construir el HTML directamente
            String html = templateEngine.process("email_verificacion", context);
            helper.setText(html, true);

            mailSender.send(mimeMessage);
            logger.info("Correo de verificación (HTML) enviado a '{}'.", to);
            return true;
        } catch (Exception e) {
            logger.error("Error al enviar correo de verificación (HTML) a '{}': {}", to, e.getMessage());
            throw new RuntimeException("Error enviando correo de verificación", e);
        }
    }

    /**
     * Envía un correo de recuperación de contraseña con una nueva contraseña en formato HTML con reintentos.
     */
    @Retryable(
            value = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public boolean enviarCorreoRecuperacionContrasenaHtml(String to, String nuevaContrasena) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Recuperación de Contraseña - Gestión de Torneos de Tenis");
            helper.setFrom("gestiontorneostenis@gmail.com"); // Remitente

            // Preparar el contexto para Thymeleaf
            Context context = new Context();
            context.setVariable("nuevaContrasena", nuevaContrasena);

            // Procesar la plantilla Thymeleaf
            String html = templateEngine.process("email_recuperacion_contrasena", context);

            helper.setText(html, true); // Indica que es HTML

            mailSender.send(mimeMessage);
            logger.info("Correo de recuperación (HTML) enviado a '{}'.", to);
            return true;
        } catch (Exception e) {
            logger.error("Error al enviar correo de recuperación (HTML) a '{}': {}", to, e.getMessage());
            throw new RuntimeException("Error enviando correo de recuperación de contraseña", e);
        }
    }
}
