// src/main/java/com/ISII/gestion_torneos_tenis/repository/JugadorRepository.java

package com.ISII.gestion_torneos_tenis.repository;

import com.ISII.gestion_torneos_tenis.model.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface JugadorRepository extends JpaRepository<Jugador, Long> {
    Optional<Jugador> findByNombreUsuario(String nombreUsuario);
    Optional<Jugador> findByCorreoElectronico(String correoElectronico);
    List<Jugador> findAllByOrderByPuntosDesc();
}
