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

    /**
     * Calcular automáticamente el ganador del partido.
     * Este método genera resultados aleatorios para cada set y determina el ganador.
     * Luego guarda el resultado en la base de datos.
     */
    public ResponseEntity<String> calcularGanadorPartido(Long emparejamientoId) {
        Optional<Emparejamiento> emparejamientoOpt = emparejamientoRepository.findById(emparejamientoId);
        if (emparejamientoOpt.isEmpty()) {
            return new ResponseEntity<>("Emparejamiento no encontrado.", HttpStatus.NOT_FOUND);
        }

        Emparejamiento emparejamiento = emparejamientoOpt.get();

        // Verificar si ya existe un resultado para este emparejamiento
        if (!resultadoRepository.findByEmparejamiento(emparejamiento).isEmpty()) {
            return new ResponseEntity<>("Ya se ha calculado el ganador para este partido.", HttpStatus.BAD_REQUEST);
        }

        // Generar resultados aleatorios para cada set
        // Supongamos que los partidos son al mejor de 3 sets

        Random random = new Random();
        int setsGanadosJugador1 = 0;
        int setsGanadosJugador2 = 0;

        Integer set1Jugador1Score = generarSet(random);
        Integer set1Jugador2Score = generarSet(random);

        if (set1Jugador1Score > set1Jugador2Score) {
            setsGanadosJugador1++;
        } else {
            setsGanadosJugador2++;
        }

        Integer set2Jugador1Score = generarSet(random);
        Integer set2Jugador2Score = generarSet(random);

        if (set2Jugador1Score > set2Jugador2Score) {
            setsGanadosJugador1++;
        } else {
            setsGanadosJugador2++;
        }

        Integer set3Jugador1Score = null;
        Integer set3Jugador2Score = null;

        if (setsGanadosJugador1 == 1 && setsGanadosJugador2 == 1) {
            set3Jugador1Score = generarSet(random);
            set3Jugador2Score = generarSet(random);

            if (set3Jugador1Score > set3Jugador2Score) {
                setsGanadosJugador1++;
            } else {
                setsGanadosJugador2++;
            }
        }

        // Determinar el ganador
        Jugador ganador;
        if (setsGanadosJugador1 > setsGanadosJugador2) {
            ganador = emparejamiento.getJugador1();
        } else {
            ganador = emparejamiento.getJugador2();
        }

        // Crear y guardar el resultado
        Resultado resultado = new Resultado();
        resultado.setEmparejamiento(emparejamiento);
        resultado.setGanador(ganador);
        resultado.setSet1Jugador1Score(set1Jugador1Score);
        resultado.setSet1Jugador2Score(set1Jugador2Score);

        if (set2Jugador1Score != null && set2Jugador2Score != null) {
            resultado.setSet2Jugador1Score(set2Jugador1Score);
            resultado.setSet2Jugador2Score(set2Jugador2Score);
        }

        if (set3Jugador1Score != null && set3Jugador2Score != null) {
            resultado.setSet3Jugador1Score(set3Jugador1Score);
            resultado.setSet3Jugador2Score(set3Jugador2Score);
        }

        // Calcular Juegos Ganados por Cada Jugador
        int juegosGanadosJugador1 = 0;
        int juegosGanadosJugador2 = 0;

        // Asumiendo que cada set representa juegos ganados
        if (resultado.getSet1Jugador1Score() != null && resultado.getSet1Jugador2Score() != null) {
            juegosGanadosJugador1 += resultado.getSet1Jugador1Score();
            juegosGanadosJugador2 += resultado.getSet1Jugador2Score();
        }

        if (resultado.getSet2Jugador1Score() != null && resultado.getSet2Jugador2Score() != null) {
            juegosGanadosJugador1 += resultado.getSet2Jugador1Score();
            juegosGanadosJugador2 += resultado.getSet2Jugador2Score();
        }

        if (resultado.getSet3Jugador1Score() != null && resultado.getSet3Jugador2Score() != null) {
            juegosGanadosJugador1 += resultado.getSet3Jugador1Score();
            juegosGanadosJugador2 += resultado.getSet3Jugador2Score();
        }

        resultado.setJuegosGanadosJugador1(juegosGanadosJugador1);
        resultado.setJuegosGanadosJugador2(juegosGanadosJugador2);

        resultadoRepository.save(resultado);


        return new ResponseEntity<>("Ganador del partido calculado y guardado exitosamente.", HttpStatus.OK);
    }

    /**
     * Genera un puntaje aleatorio válido para un set.
     * Asegura que uno de los jugadores gana el set con una diferencia de al menos 2 juegos.
     */
    private Integer generarSet(Random random) {
        int score1, score2;
        while (true) {
            score1 = random.nextInt(7); // 0 a 6
            score2 = random.nextInt(7); // 0 a 6
            if ((score1 >= 6 || score2 >= 6) && Math.abs(score1 - score2) >= 2) {
                break;
            }
        }
        return score1 > score2 ? score1 : score2;
    }

    /**
     * Guarda manualmente el resultado de un emparejamiento ingresado por el administrador.
     */
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

        // Determinar el ganador
        Jugador ganador = (setsGanadosJugador1 > setsGanadosJugador2) ? emparejamiento.getJugador1() : emparejamiento.getJugador2();

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

        resultadoIngresado.setJuegosGanadosJugador1(juegosGanadosJugador1);
        resultadoIngresado.setJuegosGanadosJugador2(juegosGanadosJugador2);

        // Guardar el resultado
        resultadoRepository.save(resultadoIngresado);

        // Actualizar puntos del ganador
        torneoService.actualizarPuntosGanador(ganador.getId(), emparejamiento.getTorneo().getPuntosAsignados());

        return new ResponseEntity<>("Resultado ingresado exitosamente.", HttpStatus.OK);
    }
}
