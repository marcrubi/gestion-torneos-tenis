// src/main/java/com/ISII/gestion_torneos_tenis/repository/ResultadoRepository.java

package com.ISII.gestion_torneos_tenis.repository;

import com.ISII.gestion_torneos_tenis.model.Resultado;
import com.ISII.gestion_torneos_tenis.model.Emparejamiento;
import com.ISII.gestion_torneos_tenis.model.Jugador;
import com.ISII.gestion_torneos_tenis.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultadoRepository extends JpaRepository<Resultado, Long> {
    List<Resultado> findByEmparejamiento(Emparejamiento emparejamiento);
    List<Resultado> findByGanador(Jugador ganador);
    List<Resultado> findByEmparejamiento_Torneo_IdAndEmparejamiento_NumeroRonda(Long torneoId, int numeroRonda);
}
