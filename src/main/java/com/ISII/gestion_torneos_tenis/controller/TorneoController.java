// src/main/java/com/ISII/gestion_torneos_tenis/controller/TorneoController.java

package com.ISII.gestion_torneos_tenis.controller;

import com.ISII.gestion_torneos_tenis.model.Torneo;
import com.ISII.gestion_torneos_tenis.model.Jugador;
import com.ISII.gestion_torneos_tenis.model.Inscripcion;
import com.ISII.gestion_torneos_tenis.repository.InscripcionRepository;
import com.ISII.gestion_torneos_tenis.service.JugadorService;
import com.ISII.gestion_torneos_tenis.service.TorneoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

import java.util.*;

@Controller
@RequestMapping("/torneos")
public class TorneoController {

    @Autowired
    private TorneoService torneoService;

    @Autowired
    private JugadorService jugadorService;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    /**
     * Visualiza los torneos disponibles para inscripción.
     */
    @GetMapping("/disponibles")
    public String visualizarTorneosDisponibles(Model model, Authentication authentication) {
        try {
            List<Torneo> listaDeTorneos = torneoService.obtenerTorneosDisponibles();
            Jugador jugador = jugadorService.obtenerJugadorPorNombreUsuario(authentication.getName());
            if (jugador == null) {
                model.addAttribute("errorMessage", "Jugador no encontrado.");
                return "error/error"; // Asegúrate de tener una plantilla error.html
            }
            // Obtener las inscripciones del jugador
            List<Inscripcion> inscripcionesJugador = inscripcionRepository.findByJugadorAndEstadoInscripcion(jugador, "Inscrito");

            // Obtener IDs de torneos en los que el jugador está inscrito
            List<Long> torneosInscritos = inscripcionesJugador.stream()
                    .map(ins -> ins.getTorneo().getId())
                    .collect(Collectors.toList());

            // Obtener número de inscripciones por torneo
            Map<Long, Integer> inscripcionesPorTorneo = new HashMap<>();
            for (Torneo torneo : listaDeTorneos) {
                int inscritos = inscripcionRepository.findByTorneo(torneo).size();
                inscripcionesPorTorneo.put(torneo.getId(), inscritos);
            }

            // Añadir la lista al modelo
            model.addAttribute("torneos", listaDeTorneos);
            model.addAttribute("jugador", jugador);
            model.addAttribute("torneosInscritos", torneosInscritos);
            model.addAttribute("inscripcionesPorTorneo", inscripcionesPorTorneo);

            return "torneos/torneos_disponibles"; // Asegúrate de que torneos_disponibles.html existe en templates
        } catch (Exception e) {
            // Manejar cualquier excepción inesperada
            model.addAttribute("errorMessage", "Ocurrió un error al cargar los torneos disponibles.");
            return "error/error"; // Asegúrate de tener una plantilla error.html
        }
    }


}
