// src/main/java/com/ISII/gestion_torneos_tenis/service/ResultadoService.java

package com.ISII.gestion_torneos_tenis.service;

import com.ISII.gestion_torneos_tenis.model.Resultado;
import com.ISII.gestion_torneos_tenis.model.Emparejamiento;
import com.ISII.gestion_torneos_tenis.model.Jugador;
import com.ISII.gestion_torneos_tenis.repository.ResultadoRepository;
import com.ISII.gestion_torneos_tenis.repository.EmparejamientoRepository;
import com.ISII.gestion_torneos_tenis.repository.JugadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ResultadoService {

    @Autowired
    private ResultadoRepository resultadoRepository;

    @Autowired
    private EmparejamientoRepository emparejamientoRepository;

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private RankingService rankingService;

    @Autowired
    private JugadorService jugadorService;

    @Autowired
    private TorneoService torneoService;

    @Transactional
    public ResponseEntity<String> calcularGanadorPartido(Long emparejamientoId) {
        Optional<Emparejamiento> emparejamientoOpt = emparejamientoRepository.findById(emparejamientoId);
        if (emparejamientoOpt.isEmpty()) {
            return new ResponseEntity<>("Emparejamiento no encontrado.", HttpStatus.NOT_FOUND);
        }

        Emparejamiento emparejamiento = emparejamientoOpt.get();

        // Obtener el resultado existente
        Optional<Resultado> resultadoOpt = resultadoRepository.findByEmparejamiento(emparejamiento).stream().findFirst();
        if (resultadoOpt.isEmpty()) {
            return new ResponseEntity<>("No se ha ingresado ningún resultado para este emparejamiento.", HttpStatus.BAD_REQUEST);
        }

        Resultado resultado = resultadoOpt.get();

        // Verificar si ya se ha calculado el ganador
        if (resultado.getGanador() != null) {
            return new ResponseEntity<>("El ganador ya ha sido calculado para este emparejamiento.", HttpStatus.BAD_REQUEST);
        }

        // Contar los sets ganados por cada jugador
        int setsGanadosJugador1 = 0;
        int setsGanadosJugador2 = 0;

        // Listar los puntajes de los sets
        Integer[][] sets = {
                {resultado.getSet1Jugador1Score(), resultado.getSet1Jugador2Score()},
                {resultado.getSet2Jugador1Score(), resultado.getSet2Jugador2Score()},
                {resultado.getSet3Jugador1Score(), resultado.getSet3Jugador2Score()},
                {resultado.getSet4Jugador1Score(), resultado.getSet4Jugador2Score()},
                {resultado.getSet5Jugador1Score(), resultado.getSet5Jugador2Score()}
        };

        for (Integer[] set : sets) {
            if (set[0] != null && set[1] != null) {
                if (set[0] > set[1]) {
                    setsGanadosJugador1++;
                } else if (set[1] > set[0]) {
                    setsGanadosJugador2++;
                } else {
                    // Empate en un set no es válido en tenis; puedes manejarlo según tus reglas
                    return new ResponseEntity<>("Empate encontrado en uno de los sets, lo cual no es válido.", HttpStatus.BAD_REQUEST);
                }
            }
        }

        // Determinar el ganador
        Jugador ganador;
        if (setsGanadosJugador1 > setsGanadosJugador2) {
            ganador = emparejamiento.getJugador1();
        } else if (setsGanadosJugador2 > setsGanadosJugador1) {
            ganador = emparejamiento.getJugador2();
        } else {
            return new ResponseEntity<>("El resultado de los sets es inconsistente o ha habido un empate en sets.", HttpStatus.BAD_REQUEST);
        }

        // Asignar el ganador al resultado
        resultado.setGanador(ganador);

        // Calcular juegos ganados por cada jugador
        int juegosGanadosJugador1 = 0;
        int juegosGanadosJugador2 = 0;

        for (Integer[] set : sets) {
            if (set[0] != null && set[1] != null) {
                juegosGanadosJugador1 += set[0];
                juegosGanadosJugador2 += set[1];
            }
        }

        resultado.setJuegosGanadosJugador1(juegosGanadosJugador1);
        resultado.setJuegosGanadosJugador2(juegosGanadosJugador2);

        // Guardar el resultado actualizado
        resultadoRepository.save(resultado);

        return new ResponseEntity<>("Ganador del partido calculado y guardado exitosamente.", HttpStatus.OK);
    }


    public ResponseEntity<String> guardarResultadoManual(Long emparejamientoId, Resultado resultadoIngresado) {
        Optional<Emparejamiento> emparejamientoOpt = emparejamientoRepository.findById(emparejamientoId);
        if (emparejamientoOpt.isEmpty()) {
            return new ResponseEntity<>("Emparejamiento no encontrado.", HttpStatus.NOT_FOUND);
        }

        Emparejamiento emparejamiento = emparejamientoOpt.get();

        // Verificar si ya existe un resultado para este emparejamiento
        if (!resultadoRepository.findByEmparejamiento(emparejamiento).isEmpty()) {
            return new ResponseEntity<>("Ya se ha ingresado un resultado para este emparejamiento.", HttpStatus.BAD_REQUEST);
        }

        // Validar que los puntajes sean consistentes (ganador tiene más sets)
        int setsGanadosJugador1 = 0;
        int setsGanadosJugador2 = 0;

        // Verificar cada set
        if (resultadoIngresado.getSet1Jugador1Score() != null && resultadoIngresado.getSet1Jugador2Score() != null) {
            if (resultadoIngresado.getSet1Jugador1Score() > resultadoIngresado.getSet1Jugador2Score()) {
                setsGanadosJugador1++;
            } else {
                setsGanadosJugador2++;
            }
        }

        if (resultadoIngresado.getSet2Jugador1Score() != null && resultadoIngresado.getSet2Jugador2Score() != null) {
            if (resultadoIngresado.getSet2Jugador1Score() > resultadoIngresado.getSet2Jugador2Score()) {
                setsGanadosJugador1++;
            } else {
                setsGanadosJugador2++;
            }
        }

        if (resultadoIngresado.getSet3Jugador1Score() != null && resultadoIngresado.getSet3Jugador2Score() != null) {
            if (resultadoIngresado.getSet3Jugador1Score() > resultadoIngresado.getSet3Jugador2Score()) {
                setsGanadosJugador1++;
            } else {
                setsGanadosJugador2++;
            }
        }

        if (resultadoIngresado.getSet4Jugador1Score() != null && resultadoIngresado.getSet4Jugador2Score() != null) {
            if (resultadoIngresado.getSet4Jugador1Score() > resultadoIngresado.getSet4Jugador2Score()) {
                setsGanadosJugador1++;
            } else {
                setsGanadosJugador2++;
            }
        }

        if (resultadoIngresado.getSet5Jugador1Score() != null && resultadoIngresado.getSet5Jugador2Score() != null) {
            if (resultadoIngresado.getSet5Jugador1Score() > resultadoIngresado.getSet5Jugador2Score()) {
                setsGanadosJugador1++;
            } else {
                setsGanadosJugador2++;
            }
        }

        // Determinar el ganador
        Jugador ganador;
        if (setsGanadosJugador1 > setsGanadosJugador2) {
            ganador = emparejamiento.getJugador1();
        } else if (setsGanadosJugador2 > setsGanadosJugador1) {
            ganador = emparejamiento.getJugador2();
        } else {
            return new ResponseEntity<>("El resultado de los sets es inconsistente o empate.", HttpStatus.BAD_REQUEST);
        }

        resultadoIngresado.setEmparejamiento(emparejamiento);
        resultadoIngresado.setGanador(ganador);

        // Calcular Juegos Ganados por Cada Jugador
        int juegosGanadosJugador1 = 0;
        int juegosGanadosJugador2 = 0;

        // Asumiendo que cada set representa juegos ganados
        if (resultadoIngresado.getSet1Jugador1Score() != null && resultadoIngresado.getSet1Jugador2Score() != null) {
            juegosGanadosJugador1 += resultadoIngresado.getSet1Jugador1Score();
            juegosGanadosJugador2 += resultadoIngresado.getSet1Jugador2Score();
        }

        if (resultadoIngresado.getSet2Jugador1Score() != null && resultadoIngresado.getSet2Jugador2Score() != null) {
            juegosGanadosJugador1 += resultadoIngresado.getSet2Jugador1Score();
            juegosGanadosJugador2 += resultadoIngresado.getSet2Jugador2Score();
        }

        if (resultadoIngresado.getSet3Jugador1Score() != null && resultadoIngresado.getSet3Jugador2Score() != null) {
            juegosGanadosJugador1 += resultadoIngresado.getSet3Jugador1Score();
            juegosGanadosJugador2 += resultadoIngresado.getSet3Jugador2Score();
        }

        if (resultadoIngresado.getSet4Jugador1Score() != null && resultadoIngresado.getSet4Jugador2Score() != null) {
            juegosGanadosJugador1 += resultadoIngresado.getSet4Jugador1Score();
            juegosGanadosJugador2 += resultadoIngresado.getSet4Jugador2Score();
        }

        if (resultadoIngresado.getSet5Jugador1Score() != null && resultadoIngresado.getSet5Jugador2Score() != null) {
            juegosGanadosJugador1 += resultadoIngresado.getSet5Jugador1Score();
            juegosGanadosJugador2 += resultadoIngresado.getSet5Jugador2Score();
        }

        resultadoIngresado.setJuegosGanadosJugador1(juegosGanadosJugador1);
        resultadoIngresado.setJuegosGanadosJugador2(juegosGanadosJugador2);

        resultadoRepository.save(resultadoIngresado);

        return new ResponseEntity<>("Resultado ingresado exitosamente.", HttpStatus.OK);
    }


}
