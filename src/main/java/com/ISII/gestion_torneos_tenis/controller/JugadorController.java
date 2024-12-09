// src/main/java/com/ISII/gestion_torneos_tenis/controller/JugadorController.java

package com.ISII.gestion_torneos_tenis.controller;

import com.ISII.gestion_torneos_tenis.dto.PasswordResetRequest;
import com.ISII.gestion_torneos_tenis.model.Jugador;
import com.ISII.gestion_torneos_tenis.model.Torneo;
import com.ISII.gestion_torneos_tenis.model.Inscripcion;
import com.ISII.gestion_torneos_tenis.model.Emparejamiento;
import com.ISII.gestion_torneos_tenis.service.*;
import com.ISII.gestion_torneos_tenis.repository.InscripcionRepository;
import com.ISII.gestion_torneos_tenis.repository.EmparejamientoRepository;
import com.ISII.gestion_torneos_tenis.repository.TorneoRepository;
import com.ISII.gestion_torneos_tenis.repository.JugadorRepository;
import com.ISII.gestion_torneos_tenis.dto.TorneoConEmparejamientosDTO;
import com.ISII.gestion_torneos_tenis.dto.EmparejamientoDTO;



import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;


import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
@Controller
public class JugadorController {

    private final JugadorService jugadorService;
    private final TorneoService torneoService;
    private final EstadisticasService estadisticasService;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private TorneoRepository torneoRepository;

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private RankingService rankingService;

    @Autowired
    private EmparejamientoRepository  emparejamientoRepository;
    @Autowired
    private EmparejamientoService emparejamientoService;


    @Autowired
    public JugadorController(JugadorService jugadorService, TorneoService torneoService, EstadisticasService estadisticasService) {
        this.jugadorService = jugadorService;
        this.torneoService = torneoService;
        this.estadisticasService = estadisticasService;
    }

    @GetMapping("/jugadores/register")
    public String mostrarRegistro(Model model) {
        model.addAttribute("jugador", new Jugador());
        return "public/register";
    }

// src/main/java/com/ISII/gestion_torneos_tenis/controller/JugadorController.java

    @PostMapping("/jugadores/register")
    public String registrarJugador(@Valid @ModelAttribute Jugador jugador, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Por favor, corrige los errores en el formulario.");
            return "public/register";
        }

        ResponseEntity<String> response = jugadorService.registerJugador(jugador);

        if (response.getStatusCode().is2xxSuccessful()) {
            model.addAttribute("successMessage", response.getBody());
            return "public/login";
        } else {
            model.addAttribute("errorMessage", response.getBody());
            return "public/register";
        }
    }


    @GetMapping("/jugadores/verificar")
    public String verificarCuenta(@RequestParam String token, Model model) {
        ResponseEntity<String> response = jugadorService.verificarCuenta(token);
        if (response.getStatusCode().is2xxSuccessful()) {
            model.addAttribute("successMessage", response.getBody());
        } else {
            model.addAttribute("errorMessage", response.getBody());
        }
        return "public/login";
    }

    @GetMapping("/jugadores/login")
    public String mostrarLogin(@RequestParam(required = false) String error, @RequestParam(required = false) String logout, Model model) {
        if (error != null) {
            if (error.equals("disabled")) {
                model.addAttribute("errorMessage", "Tu cuenta no está verificada. Por favor, verifica tu correo electrónico.");
            } else {
                model.addAttribute("errorMessage", "Nombre de usuario o contraseña incorrectos.");
            }
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Has cerrado sesión correctamente.");
        }
        return "public/login";
    }

    @GetMapping("/jugadores/recuperar")
    public String mostrarFormularioRecuperarContrasena(Model model) {
        model.addAttribute("passwordResetRequest", new PasswordResetRequest());
        return "public/recuperar_contraseña";
    }

    @PostMapping("/jugadores/recuperar")
    public String recuperarContrasena(@Valid @ModelAttribute PasswordResetRequest passwordResetRequest,
                                      org.springframework.validation.BindingResult result,
                                      Model model) {
        if (result.hasErrors()) {
            return "public/recuperar_contraseña";
        }

        ResponseEntity<String> response = jugadorService.recuperarContrasena(passwordResetRequest.getCorreoElectronico());

        if (response.getStatusCode().is2xxSuccessful()) {
            model.addAttribute("successMessage", response.getBody());
        } else {
            model.addAttribute("errorMessage", response.getBody());
        }
        return "public/recuperar_contraseña";
    }



    @GetMapping("/jugadores/perfil")
    public String mostrarPerfil(Model model, Authentication authentication) {
        String nombreUsuario = authentication.getName();
        Jugador jugador = jugadorService.obtenerJugadorPorNombreUsuario(nombreUsuario);
        model.addAttribute("jugador", jugador);
        EstadisticasService.EstadisticasJugador estadisticas = estadisticasService.calcularEstadisticas(jugador.getId());
        model.addAttribute("estadisticas", estadisticas);
        return "perfil/perfil";
    }

    @PostMapping("/jugadores/perfil")
    public String modificarPerfil(@Valid @ModelAttribute Jugador jugador, BindingResult result, Model model, Authentication authentication) {
        if (result.hasErrors()) {
            model.addAttribute("jugador", jugador);
            return "perfil/perfil";
        }

        String nombreUsuario = authentication.getName();
        Jugador jugadorActual = jugadorService.obtenerJugadorPorNombreUsuario(nombreUsuario);

        ResponseEntity<String> response = jugadorService.modificarPerfil(jugadorActual.getId(), jugador);

        if (response.getStatusCode().is2xxSuccessful()) {
            model.addAttribute("successMessage", response.getBody());
        } else {
            model.addAttribute("errorMessage", response.getBody());
        }

        Jugador jugadorActualizado = jugadorService.obtenerJugadorPorNombreUsuario(nombreUsuario);
        model.addAttribute("jugador", jugadorActualizado);
        EstadisticasService.EstadisticasJugador estadisticas = estadisticasService.calcularEstadisticas(jugadorActual.getId());
        model.addAttribute("estadisticas", estadisticas);

        model.addAttribute("successMessage", "Perfil actualizado exitosamente.");

        return "perfil/perfil";
    }



    @PostMapping("/torneos/{id}/inscribir")
    public String inscribirATorneo(@PathVariable("id") Long torneoId,
                                   RedirectAttributes redirectAttributes,
                                   Authentication authentication) {
        String nombreUsuario = authentication.getName();
        Jugador jugador = jugadorService.obtenerJugadorPorNombreUsuario(nombreUsuario);

        Optional<Torneo> torneoOpt = torneoRepository.findById(torneoId);
        if (torneoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Torneo no encontrado.");
            return "redirect:/torneos/disponibles"; // Cambio aquí
        }

        Torneo torneo = torneoOpt.get();

        if (LocalDateTime.now().isAfter(torneo.getFechaLimiteInscripcion())) {
            redirectAttributes.addFlashAttribute("errorMessage", "La fecha límite de inscripción ha pasado para este torneo.");
            return "redirect:/torneos/disponibles"; // Cambio aquí
        }

        Optional<Inscripcion> inscripcionOpt = inscripcionRepository.findByJugadorAndTorneo(jugador, torneo);
        if (inscripcionOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ya estás inscrito en este torneo.");
            return "redirect:/torneos/disponibles"; // Cambio aquí
        }


        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setJugador(jugador);
        inscripcion.setTorneo(torneo);
        inscripcion.setEstadoInscripcion("Inscrito");
        inscripcion.setFechaInscripcion(LocalDateTime.now());

        inscripcionRepository.save(inscripcion);

        redirectAttributes.addFlashAttribute("successMessage", "Inscripción realizada exitosamente al torneo: " + torneo.getNombreTorneo());
        return "redirect:/torneos/disponibles"; // Cambio aquí
    }



    @GetMapping("/jugadores/mis-torneos")
    public String mostrarMisTorneos(Model model, Authentication authentication) {
        String nombreUsuario = authentication.getName();
        Jugador jugador = jugadorService.obtenerJugadorPorNombreUsuario(nombreUsuario);

        // Obtener todas las inscripciones del jugador
        List<Inscripcion> inscripciones = inscripcionRepository.findByJugador(jugador);
        List<TorneoConEmparejamientosDTO> torneosData = new ArrayList<>();

        for (Inscripcion inscripcion : inscripciones) {
            Torneo torneo = inscripcion.getTorneo();
            String estadoInscripcion = inscripcion.getEstadoInscripcion();

            // Obtener la posición del jugador en el ranking
            int posicionRankingJugador = rankingService.obtenerPosicionRanking(jugador.getId());

            // Obtener todos los emparejamientos donde el jugador es jugador1 o jugador2 en este torneo
            List<Emparejamiento> emparejamientos = emparejamientoRepository.findByTorneoAndJugador(torneo, jugador);

            // Crear una lista de EmparejamientoDTO
            List<EmparejamientoDTO> emparejamientosDTO = new ArrayList<>();
            for (Emparejamiento emparejamiento : emparejamientos) {
                // Obtener la posición del ranking para jugador1
                int posicionRankingJugador1 = rankingService.obtenerPosicionRanking(emparejamiento.getJugador1().getId());

                // Obtener la posición del ranking para jugador2, si existe
                int posicionRankingJugador2 = 0;
                if (emparejamiento.getJugador2() != null) {
                    posicionRankingJugador2 = rankingService.obtenerPosicionRanking(emparejamiento.getJugador2().getId());
                }

                // Crear el DTO para este emparejamiento
                EmparejamientoDTO emparejamientoDTO = new EmparejamientoDTO(
                        emparejamiento,
                        posicionRankingJugador1,
                        posicionRankingJugador2
                );

                emparejamientosDTO.add(emparejamientoDTO);
            }

            // Crear el DTO para este torneo
            TorneoConEmparejamientosDTO torneoDTO = new TorneoConEmparejamientosDTO(
                    torneo,
                    estadoInscripcion,
                    posicionRankingJugador,
                    emparejamientosDTO
            );

            torneosData.add(torneoDTO);
        }

        model.addAttribute("torneosData", torneosData);

        return "torneos/mis_torneos";
    }

    @GetMapping("/jugadores/mis-resultados")
    public String mostrarMisResultados(Model model, Authentication authentication) {
        // Obtener el jugador autenticado
        String nombreUsuario = authentication.getName();
        Jugador jugador = jugadorService.obtenerJugadorPorNombreUsuario(nombreUsuario);

        // Recuperar las inscripciones del jugador
        List<Inscripcion> inscripciones = inscripcionRepository.findByJugador(jugador);

        // Utilizar un Set para evitar duplicados
        Set<Torneo> torneosFinalizadosSet = inscripciones.stream()
                .map(Inscripcion::getTorneo)
                .filter(t -> "Finalizado".equalsIgnoreCase(t.getEstadoTorneo()))
                .collect(Collectors.toSet());

        // Convertir el Set en una lista
        List<Torneo> torneosFinalizados = new ArrayList<>(torneosFinalizadosSet);

        // Mapas para almacenar posición final y puntos obtenidos por torneo
        Map<Long, Integer> posicionPorTorneo = new HashMap<>();
        Map<Long, Integer> puntosPorTorneo = new HashMap<>();

        // Recorrer los torneos finalizados y calcular posición y puntos
        for (Torneo t : torneosFinalizados) {
            // Recuperar la inscripción correspondiente al jugador
            Inscripcion inscripcion = inscripcionRepository.findByJugadorAndTorneo(jugador, t)
                    .orElseThrow(() -> new RuntimeException("Inscripción no encontrada para jugador y torneo."));

            // Recuperar la posición final directamente de la inscripción
            Integer posicionFinal = inscripcion.getPosicionFinal();

            if (posicionFinal != null && posicionFinal > 0) { // Solo agregar si la posición es válida
                posicionPorTorneo.put(t.getId(), posicionFinal);

                // Calcular puntos asignados según la posición final
                int puntos = calcularPuntosPorPosicion(posicionFinal);
                puntosPorTorneo.put(t.getId(), puntos);
            } else {
                // Si no hay posición, registrar con valores predeterminados
                posicionPorTorneo.put(t.getId(), 0);
                puntosPorTorneo.put(t.getId(), 0);
            }
        }

        // Añadir los atributos al modelo para la vista
        model.addAttribute("jugador", jugador);
        model.addAttribute("torneosFinalizados", torneosFinalizados);
        model.addAttribute("posicionPorTorneo", posicionPorTorneo);
        model.addAttribute("puntosPorTorneo", puntosPorTorneo);

        return "torneos/mis_resultados";
    }


    /**
     * Calcula los puntos obtenidos según la posición final en el torneo.
     */
    private int calcularPuntosPorPosicion(int posicion) {
        switch (posicion) {
            case 1: return 2000;
            case 2: return 1500;
            case 3: return 1000;
            case 4: return 500;
            case 5: return 475;
            case 6: return 450;
            case 7: return 425;
            case 8: return 400;
            case 9: return 375;
            case 10:return 350;
            case 11:return 325;
            case 12:return 300;
            case 13:return 275;
            case 14:return 250;
            case 15:return 225;
            case 16:return 200;
            default:
                // A partir del 17º, asignar 200 puntos
                return 200;
        }
    }


    @GetMapping("/jugadores/estadisticas")
    public String mostrarEstadisticas(Model model, Authentication authentication) {
        String nombreUsuario = authentication.getName();
        Jugador jugador = jugadorService.obtenerJugadorPorNombreUsuario(nombreUsuario);
        EstadisticasService.EstadisticasJugador estadisticas = estadisticasService.calcularEstadisticas(jugador.getId());

        // Obtener la posición en el ranking
        int posicionRanking = rankingService.obtenerPosicionRanking(jugador.getId());

        // Asignar la posición al objeto de estadísticas
        estadisticas.setPosicionRanking(posicionRanking); // Asegúrate de que 'EstadisticasJugador' tenga este método

        model.addAttribute("jugador", jugador);
        model.addAttribute("estadisticas", estadisticas);
        model.addAttribute("rankingJugadores", rankingService.obtenerRankingGlobal()); // Si necesitas más datos del ranking

        return "perfil/estadisticas";
    }

    @GetMapping("/ranking/global")
    public String mostrarRankingGlobal(Model model) {
        Map<Long, Integer> rankingOrdenado = rankingService.obtenerRankingGlobal();

        // Crear una lista de mapas para pasar a la vista
        List<Map<String, Object>> rankingData = new ArrayList<>();
        int posicion = 1;
        for (Map.Entry<Long, Integer> entry : rankingOrdenado.entrySet()) {
            Long jugadorId = entry.getKey();
            Integer puntos = entry.getValue();
            Jugador jug = jugadorRepository.findById(jugadorId).orElse(null);
            if (jug != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("jugador", jug);
                data.put("puntos", puntos);
                data.put("posicion", posicion++);
                rankingData.add(data);
            }
        }

        model.addAttribute("ranking", rankingData);
        return "torneos/ranking_global"; // Asegúrate de tener la plantilla 'ranking_global.html'
    }


}
