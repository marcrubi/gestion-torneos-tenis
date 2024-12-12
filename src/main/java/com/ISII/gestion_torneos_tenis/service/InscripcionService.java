// src/main/java/com/ISII/gestion_torneos_tenis/service/InscripcionService.java

package com.ISII.gestion_torneos_tenis.service;

import com.ISII.gestion_torneos_tenis.model.Inscripcion;
import com.ISII.gestion_torneos_tenis.model.Jugador;
import com.ISII.gestion_torneos_tenis.model.Torneo;
import com.ISII.gestion_torneos_tenis.repository.InscripcionRepository;
import com.ISII.gestion_torneos_tenis.repository.JugadorRepository;
import com.ISII.gestion_torneos_tenis.repository.TorneoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InscripcionService {

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private TorneoRepository torneoRepository;

    /**
     * Inscribir un jugador en un torneo.
     */
    public String inscribirJugadorEnTorneo(Long jugadorId, Long torneoId) {
        Optional<Jugador> jugadorOpt = jugadorRepository.findById(jugadorId);
        Optional<Torneo> torneoOpt = torneoRepository.findById(torneoId);

        if (jugadorOpt.isEmpty()) {
            return "Jugador no encontrado.";
        }

        if (torneoOpt.isEmpty()) {
            return "Torneo no encontrado.";
        }

        Jugador jugador = jugadorOpt.get();
        Torneo torneo = torneoOpt.get();

        // Verificar si la inscripción está abierta
        if (torneo.getFechaLimiteInscripcion().isBefore(LocalDateTime.now())) {
            return "La inscripción para este torneo ya ha finalizado.";
        }

        // Verificar si el jugador ya está inscrito
        Optional<Inscripcion> inscripcionOpt = inscripcionRepository.findByJugadorAndTorneo(jugador, torneo);
        if (inscripcionOpt.isPresent()) {
            return "Ya estás inscrito en este torneo.";
        }

        // Verificar si el torneo ya alcanzó el número máximo de jugadores
        List<Inscripcion> inscripciones = inscripcionRepository.findByTorneo(torneo);
        if (inscripciones.size() >= torneo.getNumeroMaxJugadores()) {
            return "El torneo ya ha alcanzado el número máximo de jugadores.";
        }

        // Crear una nueva inscripción
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setJugador(jugador);
        inscripcion.setTorneo(torneo);
        inscripcion.setEstadoInscripcion("Inscrito");
        inscripcion.setFechaInscripcion(LocalDateTime.now());

        inscripcionRepository.save(inscripcion);

        return "Inscripción exitosa al torneo.";
    }
}
