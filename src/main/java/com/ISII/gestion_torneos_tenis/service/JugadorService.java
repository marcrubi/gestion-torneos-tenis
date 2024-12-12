package com.ISII.gestion_torneos_tenis.service;

import com.ISII.gestion_torneos_tenis.model.Jugador;
import com.ISII.gestion_torneos_tenis.model.VerificationToken;
import com.ISII.gestion_torneos_tenis.repository.JugadorRepository;
import com.ISII.gestion_torneos_tenis.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class JugadorService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(JugadorService.class);

    private final JugadorRepository jugadorRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder; // Se inyectará desde AppConfig
    private final EmailService emailService;

    @Autowired
    public JugadorService(JugadorRepository jugadorRepository,
                          VerificationTokenRepository verificationTokenRepository,
                          PasswordEncoder passwordEncoder,
                          EmailService emailService) {
        this.jugadorRepository = jugadorRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Registra un nuevo jugador y envía un correo de verificación.
     */
// src/main/java/com/ISII/gestion_torneos_tenis/service/JugadorService.java

    public ResponseEntity<String> registerJugador(Jugador jugador) {
        // Verificar si el nombre de usuario ya existe
        if (jugadorRepository.findByNombreUsuario(jugador.getNombreUsuario()).isPresent()) {
            return new ResponseEntity<>("El nombre de usuario ya está en uso.", HttpStatus.BAD_REQUEST);
        }

        // Encriptar la contraseña
        String encodedPassword = passwordEncoder.encode(jugador.getContrasena());
        jugador.setContrasena(encodedPassword);
        jugador.setEstadoVerificacion(false); // No verificado por defecto

        // Guardar el jugador
        Jugador nuevoJugador = jugadorRepository.save(jugador);

        // Generar enlace de verificación
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, nuevoJugador, LocalDateTime.now().plusHours(24));
        verificationTokenRepository.save(verificationToken);

        // Crear enlace de verificación
        String enlaceVerificacion = "http://localhost:8080/jugadores/verificar?token=" + token;

        try {
            // Enviar correo
            boolean emailEnviado = emailService.enviarCorreoVerificacionHtml(nuevoJugador.getCorreoElectronico(), enlaceVerificacion);
            if (emailEnviado) {
                return new ResponseEntity<>("Registro exitoso. Por favor, verifica tu correo.", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Registro exitoso, pero no se pudo enviar el correo de verificación.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            // Loguear el error y devolver un mensaje de error
            logger.error("Error enviando el correo de verificación: {}", e.getMessage());
            return new ResponseEntity<>("Registro exitoso, pero ocurrió un error al enviar el correo de verificación.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Verifica la cuenta del jugador utilizando el token enviado por correo.
     */
    public ResponseEntity<String> verificarCuenta(String token) {
        Optional<VerificationToken> tokenOpt = verificationTokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            logger.warn("Token inválido: '{}'.", token);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token de verificación inválido.");
        }

        VerificationToken verificationToken = tokenOpt.get();

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            logger.warn("Token expirado: '{}'.", token);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token de verificación expirado.");
        }

        Jugador jugador = verificationToken.getJugador();
        jugador.setEstadoVerificacion(true);
        jugadorRepository.save(jugador);

        verificationTokenRepository.delete(verificationToken); // Limpiar el token
        logger.info("Cuenta verificada exitosamente para '{}'.", jugador.getNombreUsuario());

        return ResponseEntity.ok("Cuenta verificada exitosamente. Puedes iniciar sesión ahora.");
    }

    /**
     * Recupera la contraseña del jugador y envía una nueva contraseña por correo.
     */
// src/main/java/com/ISII/gestion_torneos_tenis/service/JugadorService.java

    public ResponseEntity<String> recuperarContrasena(String correoElectronico) {
        Optional<Jugador> jugadorOpt = jugadorRepository.findByCorreoElectronico(correoElectronico);

        if (jugadorOpt.isEmpty()) {
            logger.warn("Correo no encontrado: '{}'.", correoElectronico);
            return ResponseEntity.ok("Si el correo está registrado, se ha enviado una nueva contraseña.");
        }

        Jugador jugador = jugadorOpt.get();
        String nuevaContrasena = generarContrasenaAleatoria(10); // Generar nueva contraseña

        jugador.setContrasena(passwordEncoder.encode(nuevaContrasena));
        jugadorRepository.save(jugador);

        // **Usar el método correcto para enviar la nueva contraseña**
        boolean emailEnviado = emailService.enviarCorreoRecuperacionContrasenaHtml(correoElectronico, nuevaContrasena);
        if (emailEnviado) {
            logger.info("Correo de recuperación enviado a '{}'.", correoElectronico);
            return ResponseEntity.ok("Se ha enviado una nueva contraseña a tu correo electrónico.");
        } else {
            logger.error("Error al enviar correo de recuperación a '{}'.", correoElectronico);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocurrió un error al enviar la nueva contraseña.");
        }
    }

    /**
     * Genera una contraseña aleatoria de la longitud especificada.
     */
    private String generarContrasenaAleatoria(int longitud) {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*!";
        StringBuilder contrasena = new StringBuilder();
        for (int i = 0; i < longitud; i++) {
            int index = (int) (Math.random() * caracteres.length());
            contrasena.append(caracteres.charAt(index));
        }
        return contrasena.toString();
    }

    /**
     * Implementación de UserDetailsService para Spring Security.
     * Ahora todos los usuarios serán ROLE_USER por defecto.
     */

    /**
     * Obtiene un jugador por su nombre de usuario.
     */
    public Jugador obtenerJugadorPorNombreUsuario(String nombreUsuario) {
        return jugadorRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + nombreUsuario));
    }

    /**
     * Obtiene un jugador por su ID.
     */
    public Jugador obtenerJugadorPorId(Long id) {
        return jugadorRepository.findById(id).orElse(null);
    }

    /**
     * Modifica el perfil del jugador.
     */
    public ResponseEntity<String> modificarPerfil(Long id, Jugador jugador) {
        Optional<Jugador> jugadorOpt = jugadorRepository.findById(id);
        if (jugadorOpt.isEmpty()) {
            logger.warn("Actualización de perfil fallida: Jugador no encontrado con ID '{}'.", id);
            return new ResponseEntity<>("Jugador no encontrado.", HttpStatus.BAD_REQUEST);
        }

        Jugador jugadorActual = jugadorOpt.get();

        // Verificar si el nuevo nombre de usuario ya está en uso por otro jugador
        if (!jugadorActual.getNombreUsuario().equals(jugador.getNombreUsuario())) {
            if (jugadorRepository.findByNombreUsuario(jugador.getNombreUsuario()).isPresent()) {
                return new ResponseEntity<>("El nombre de usuario ya está en uso.", HttpStatus.BAD_REQUEST);
            }
        }

        jugadorActual.setNombreUsuario(jugador.getNombreUsuario());
        jugadorActual.setNombre(jugador.getNombre());
        jugadorActual.setApellidos(jugador.getApellidos());
        jugadorActual.setCorreoElectronico(jugador.getCorreoElectronico());

        // Solo actualizar la contraseña si se proporciona una nueva
        if (jugador.getContrasena() != null && !jugador.getContrasena().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(jugador.getContrasena());
            jugadorActual.setContrasena(encodedPassword);
            logger.debug("Contraseña actualizada para '{}'.", jugadorActual.getNombreUsuario());
        }

        jugadorActual.setTelefono(jugador.getTelefono());

        jugadorRepository.save(jugadorActual);
        logger.info("Perfil actualizado exitosamente para '{}'.", jugadorActual.getNombreUsuario());

        return new ResponseEntity<>("Perfil actualizado exitosamente.", HttpStatus.OK);
    }

    @Override
    public UserDetails loadUserByUsername(String nombreUsuario) throws UsernameNotFoundException {
        logger.debug("Intentando cargar usuario: '{}'", nombreUsuario);
        Jugador jugador = jugadorRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> {
                    logger.warn("Usuario no encontrado con nombre de usuario: '{}'", nombreUsuario);
                    return new UsernameNotFoundException("Usuario no encontrado con nombre de usuario: " + nombreUsuario);
                });

        if (!jugador.isEstadoVerificacion()) {
            logger.warn("Usuario '{}' no verificado.", nombreUsuario);
            throw new DisabledException("Cuenta no verificada. Por favor, verifica tu correo electrónico.");
        }

        logger.debug("Usuario '{}' cargado exitosamente.", nombreUsuario);
        if (jugador.isAdmin()) {
            return User.builder()
                    .username(jugador.getNombreUsuario())
                    .password(jugador.getContrasena())
                    .roles("ADMIN","USER")
                    .build();
        } else {
            return User.builder()
                    .username(jugador.getNombreUsuario())
                    .password(jugador.getContrasena())
                    .roles("USER")
                    .build();
        }
    }

    public Jugador actualizarPuntos(Long idJugador, int puntos) {
        Jugador jugador = jugadorRepository.findById(idJugador)
                .orElseThrow(() -> new UsernameNotFoundException("Jugador no encontrado con ID: " + idJugador));
        jugador.setPuntos(jugador.getPuntos() + puntos);
        return jugadorRepository.save(jugador);
    }

}
