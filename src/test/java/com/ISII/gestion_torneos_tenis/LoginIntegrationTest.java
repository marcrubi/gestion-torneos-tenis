// src/test/java/com/ISII/gestion_torneos_tenis/LoginIntegrationTest.java
/*
package com.ISII.gestion_torneos_tenis;

import com.ISII.gestion_torneos_tenis.model.Jugador;
import com.ISII.gestion_torneos_tenis.repository.JugadorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Asegúrate de tener un perfil 'test' configurado
public class LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        // Limpiar la base de datos antes de cada prueba
        jugadorRepository.deleteAll();

        // Crear un usuario verificado
        Jugador jugadorVerificado = new Jugador();
        jugadorVerificado.setNombreUsuario("usuarioVerificado");
        jugadorVerificado.setContrasena(passwordEncoder.encode("password123"));
        jugadorVerificado.setCorreoElectronico("verificado@example.com");
        jugadorVerificado.setNombre("Usuario");
        jugadorVerificado.setApellidos("Verificado");
        jugadorVerificado.setTelefono("1234567890");
        jugadorVerificado.setEstadoVerificacion(true); // Usuario verificado
        jugadorRepository.save(jugadorVerificado);

        // Crear un usuario no verificado
        Jugador jugadorNoVerificado = new Jugador();
        jugadorNoVerificado.setNombreUsuario("usuarioNoVerificado");
        jugadorNoVerificado.setContrasena(passwordEncoder.encode("password123"));
        jugadorNoVerificado.setCorreoElectronico("noverificado@example.com");
        jugadorNoVerificado.setNombre("Usuario");
        jugadorNoVerificado.setApellidos("No Verificado");
        jugadorNoVerificado.setTelefono("0987654321");
        jugadorNoVerificado.setEstadoVerificacion(false); // Usuario no verificado
        jugadorRepository.save(jugadorNoVerificado);
    }

    @Test
    public void testLoginConUsuarioVerificado() throws Exception {
        // Configuración previa, como crear un usuario verificado

        mockMvc.perform(post("/jugadores/login")
                        .param("nombreUsuario", "hola")
                        .param("contrasena", "password")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main")); // Esta línea está causando el fallo
    }

    @Test
    public void testLoginConUsuarioNoVerificado() throws Exception {
        mockMvc.perform(post("/jugadores/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nombreUsuario", "usuarioNoVerificado")
                        .param("contrasena", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/jugadores/login?error=true"));
    }

    @Test
    public void testLoginConCredencialesIncorrectas() throws Exception {
        mockMvc.perform(post("/jugadores/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nombreUsuario", "usuarioVerificado")
                        .param("contrasena", "wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/jugadores/login?error=true"));
    }

    @Test
    public void testLoginConUsuarioInexistente() throws Exception {
        mockMvc.perform(post("/jugadores/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nombreUsuario", "usuarioInexistente")
                        .param("contrasena", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/jugadores/login?error=true"));
    }
}
*/