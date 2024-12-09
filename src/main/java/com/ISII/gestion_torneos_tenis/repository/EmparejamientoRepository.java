// src/main/java/com/ISII/gestion_torneos_tenis/repository/EmparejamientoRepository.java

package com.ISII.gestion_torneos_tenis.repository;

import com.ISII.gestion_torneos_tenis.model.Emparejamiento;
import com.ISII.gestion_torneos_tenis.model.Jugador;
import com.ISII.gestion_torneos_tenis.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface EmparejamientoRepository extends JpaRepository<Emparejamiento, Long> {
    List<Emparejamiento> findByTorneo(Torneo torneo);
    List<Emparejamiento> findByTorneo_IdAndNumeroRonda(Long torneoId, int ronda);
    List<Emparejamiento> findByTorneo_Id(Long torneoId);
    @Query("SELECT e FROM Emparejamiento e WHERE e.torneo = :torneo AND (e.jugador1 = :jugador OR e.jugador2 = :jugador)")
    List<Emparejamiento> findByTorneoAndJugador(@Param("torneo") Torneo torneo, @Param("jugador") Jugador jugador);
}
