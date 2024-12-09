// src/main/java/com/ISII/gestion_torneos_tenis/repository/TorneoRepository.java

package com.ISII.gestion_torneos_tenis.repository;

import com.ISII.gestion_torneos_tenis.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TorneoRepository extends JpaRepository<Torneo, Long> {
    List<Torneo> findByEstadoTorneo(String estadoTorneo);
}
