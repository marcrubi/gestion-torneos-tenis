// src/main/java/com/ISII/gestion_torneos_tenis/repository/InscripcionRepository.java

package com.ISII.gestion_torneos_tenis.repository;

import com.ISII.gestion_torneos_tenis.model.Inscripcion;
import com.ISII.gestion_torneos_tenis.model.Jugador;
import com.ISII.gestion_torneos_tenis.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    List<Inscripcion> findByJugador(Jugador jugador);
    List<Inscripcion> findByJugadorAndEstadoInscripcion(Jugador jugador, String estadoInscripcion);
    Optional<Inscripcion> findByJugadorAndTorneo(Jugador jugador, Torneo torneo);
    List<Inscripcion> findByTorneoAndEstadoInscripcion(Torneo torneo, String estadoInscripcion);
    List<Inscripcion> findByTorneo(Torneo torneo);
    List<Inscripcion> findByTorneoIdAndEstadoInscripcion(Long torneoId, String estadoInscripcion);
}
