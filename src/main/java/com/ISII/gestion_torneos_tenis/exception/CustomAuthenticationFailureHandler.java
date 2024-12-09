// src/main/java/com/ISII/gestion_torneos_tenis/exception/CustomAuthenticationFailureHandler.java

package com.ISII.gestion_torneos_tenis.exception;

import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {
        // Manejar la autenticación fallida
        response.sendRedirect("/login?error");
    }
}
