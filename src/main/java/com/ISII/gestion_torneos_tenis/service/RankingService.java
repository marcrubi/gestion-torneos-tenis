// src/main/java/com/ISII/gestion_torneos_tenis/service/RankingService.java

package com.ISII.gestion_torneos_tenis.service;

import com.ISII.gestion_torneos_tenis.model.Jugador;
import com.ISII.gestion_torneos_tenis.model.Resultado;
import com.ISII.gestion_torneos_tenis.model.Torneo;
import com.ISII.gestion_torneos_tenis.repository.JugadorRepository;
import com.ISII.gestion_torneos_tenis.repository.ResultadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RankingService {

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private ResultadoRepository resultadoRepository;

    /**
     * Calcula el ranking global de los jugadores.
     * Retorna un mapa con jugadorId y puntos, ordenado de mayor a menor puntos.
     */
    public Map<Long, Integer> obtenerRankingGlobal() {
        Map<Long, Integer> puntosPorJugador = new HashMap<>();

        // Obtener todos los jugadores
        List<Jugador> todosJugadores = jugadorRepository.findAll();

        // Asegurar que todos los jugadores estén en el mapa, incluso con 0 puntos
        for (Jugador jugador : todosJugadores) {
            puntosPorJugador.putIfAbsent(jugador.getId(), jugador.getPuntos());
        }

        // Ordenar el mapa de mayor a menor puntos
        Map<Long, Integer> rankingOrdenado = puntosPorJugador.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        return rankingOrdenado;
    }

    /**
     * Obtiene la posición de ranking de un jugador específico.
     * Retorna -1 si el jugador no se encuentra en el ranking.
     */
    public int obtenerPosicionRanking(Long jugadorId) {
        Map<Long, Integer> rankingOrdenado = obtenerRankingGlobal();

        int posicion = 1;
        for (Long id : rankingOrdenado.keySet()) {
            if (id.equals(jugadorId)) {
                return posicion;
            }
            posicion++;
        }

        return -1; // Indica que no se encontró la posición
    }
}
