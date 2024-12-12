// src/main/java/com/ISII/gestion_torneos_tenis/service/EmparejamientoService.java

package com.ISII.gestion_torneos_tenis.service;

import com.ISII.gestion_torneos_tenis.model.Emparejamiento;
import com.ISII.gestion_torneos_tenis.model.Jugador;
import com.ISII.gestion_torneos_tenis.model.Resultado;
import com.ISII.gestion_torneos_tenis.model.Torneo;
import com.ISII.gestion_torneos_tenis.model.Inscripcion;
import com.ISII.gestion_torneos_tenis.repository.EmparejamientoRepository;
import com.ISII.gestion_torneos_tenis.repository.InscripcionRepository;
import com.ISII.gestion_torneos_tenis.repository.ResultadoRepository;
import com.ISII.gestion_torneos_tenis.repository.TorneoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class EmparejamientoService {

    @Autowired
    private EmparejamientoRepository emparejamientoRepository;

    @Autowired
    private TorneoRepository torneoRepository;

    @Autowired
    private ResultadoRepository resultadoRepository;

    @Autowired
    private JugadorService jugadorService;

    @Autowired
    private ResultadoService resultadoService;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    public ResponseEntity<String> generarEmparejamientos(Long torneoId, int ronda) {
        System.out.println("generarEmparejamientos: Iniciando generación de emparejamientos para torneo ID " + torneoId + ", ronda " + ronda);

        Optional<Torneo> torneoOpt = torneoRepository.findById(torneoId);
        if (torneoOpt.isEmpty()) {
            System.out.println("generarEmparejamientos: Torneo no encontrado, ID: " + torneoId);
            return ResponseEntity.badRequest().body("Torneo no encontrado.");
        }
        Torneo torneo = torneoOpt.get();

        if (ronda < 1) {
            System.out.println("generarEmparejamientos: La ronda debe ser al menos 1.");
            return ResponseEntity.badRequest().body("La ronda debe ser al menos 1.");
        }

        if (ronda == 1) {
            // Generar emparejamientos para la primera ronda usando los jugadores confirmados
            List<Jugador> jugadores = inscripcionRepository.findByTorneoIdAndEstadoInscripcion(torneoId, "Confirmada")
                    .stream()
                    .map(Inscripcion::getJugador)
                    .collect(Collectors.toList());

            if (jugadores.size() < 2) {
                return ResponseEntity.badRequest().body("No hay suficientes jugadores para generar emparejamientos en la primera ronda.");
            }

            // Mezclar aleatoriamente los jugadores para crear emparejamientos
            Collections.shuffle(jugadores);
            List<Emparejamiento> nuevosEmparejamientos = new ArrayList<>();

            int totalJugadores = jugadores.size();
            Jugador jugadorConBye = null;

            if (totalJugadores % 2 != 0) {
                // Si es impar, asignar un BYE al último jugador
                jugadorConBye = jugadores.remove(totalJugadores - 1);
                totalJugadores--;
                System.out.println("generarEmparejamientos: Jugador con BYE: ID " + jugadorConBye.getId() + ", Nombre: " + jugadorConBye.getNombreUsuario());
            }

            // Crear emparejamientos de 2 en 2
            for (int i = 0; i < totalJugadores; i += 2) {
                Jugador j1 = jugadores.get(i);
                Jugador j2 = jugadores.get(i + 1);

                Emparejamiento emp = new Emparejamiento();
                emp.setTorneo(torneo);
                emp.setNumeroRonda(ronda);
                emp.setJugador1(j1);
                emp.setJugador2(j2);
                emp.setFechaHoraPartido(LocalDateTime.now().plusDays(ronda)); // Puedes ajustar la lógica de fecha/hora según tu necesidad

                emparejamientoRepository.save(emp);
                nuevosEmparejamientos.add(emp);

                System.out.println("generarEmparejamientos: Emparejamiento creado - Ronda " + ronda + ": Jugador " + j1.getNombreUsuario() + " vs " + j2.getNombreUsuario());
            }

            // Manejar BYE
            if (jugadorConBye != null) {
                Emparejamiento empBye = new Emparejamiento();
                empBye.setTorneo(torneo);
                empBye.setNumeroRonda(ronda);
                empBye.setJugador1(jugadorConBye);
                empBye.setJugador2(null);
                empBye.setFechaHoraPartido(LocalDateTime.now().plusDays(ronda));

                emparejamientoRepository.save(empBye);
                nuevosEmparejamientos.add(empBye);

                // Resultado automático para el BYE
                Resultado resultadoBye = new Resultado();
                resultadoBye.setEmparejamiento(empBye);
                resultadoBye.setGanador(jugadorConBye);
                resultadoBye.setSet1Jugador1Score(0);
                resultadoBye.setSet1Jugador2Score(0);
                resultadoBye.setSet2Jugador1Score(0);
                resultadoBye.setSet2Jugador2Score(0);
                resultadoBye.setSet3Jugador1Score(0);
                resultadoBye.setSet3Jugador2Score(0);
                resultadoBye.setJuegosGanadosJugador1(0);
                resultadoBye.setJuegosGanadosJugador2(0);
                resultadoRepository.save(resultadoBye);

                System.out.println("generarEmparejamientos: Resultado automático registrado para el BYE del jugador " + jugadorConBye.getNombreUsuario());

                }

            // Actualizar estado del torneo si es la primera ronda
            torneo.setEstadoTorneo("Emparejamientos Generados");
            torneoRepository.save(torneo);
            System.out.println("generarEmparejamientos: Estado del torneo actualizado a 'Emparejamientos Generados'.");

            System.out.println("generarEmparejamientos: Generación de emparejamientos para la ronda " + ronda + " completada.");
            return ResponseEntity.ok("Emparejamientos generados exitosamente para la ronda " + ronda + ".");
        } else {
            // Para rondas posteriores, verificar la ronda anterior
            int rondaAnterior = ronda - 1;

            List<Resultado> resultadosRondaAnterior = resultadoRepository.findByEmparejamiento_Torneo_IdAndEmparejamiento_NumeroRonda(torneoId, rondaAnterior);

            System.out.println("generarEmparejamientos: Resultados encontrados para ronda " + rondaAnterior + ": " + resultadosRondaAnterior.size());
            if (resultadosRondaAnterior.isEmpty()) {
                System.out.println("generarEmparejamientos: No hay resultados de la ronda anterior.");
                return ResponseEntity.badRequest().body("No hay resultados de la ronda anterior.");
            }

            // Obtener ganadores
            List<Jugador> ganadores = resultadosRondaAnterior.stream()
                    .map(Resultado::getGanador)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            System.out.println("generarEmparejamientos: Ganadores de la ronda anterior: " + ganadores.size());
            ganadores.forEach(j -> System.out.println(" - Jugador ID: " + j.getId() + ", Nombre: " + j.getNombreUsuario()));

            if (ganadores.size() <= 1) {
                // Si hay 1 o 0 ganadores, el torneo puede haber finalizado
                System.out.println("generarEmparejamientos: No hay suficientes ganadores para generar la siguiente ronda.");
                return ResponseEntity.ok("No hay suficientes jugadores para generar la siguiente ronda. El torneo puede haber finalizado.");
            }

            // Mezclar para aleatorizar
            Collections.shuffle(ganadores);
            System.out.println("generarEmparejamientos: Ganadores mezclados para emparejamiento aleatorio.");

            List<Emparejamiento> nuevosEmparejamientos = new ArrayList<>();

            int totalJugadores = ganadores.size();
            Jugador jugadorConBye = null;

            if (totalJugadores % 2 != 0) {
                // Si es impar, asignar un BYE al último jugador
                jugadorConBye = ganadores.remove(totalJugadores - 1);
                totalJugadores--;
                System.out.println("generarEmparejamientos: Jugador con BYE: ID " + jugadorConBye.getId() + ", Nombre: " + jugadorConBye.getNombreUsuario());
            }

            // Crear emparejamientos de 2 en 2
            for (int i = 0; i < totalJugadores; i += 2) {
                Jugador j1 = ganadores.get(i);
                Jugador j2 = ganadores.get(i + 1);

                Emparejamiento emp = new Emparejamiento();
                emp.setTorneo(torneo);
                emp.setNumeroRonda(ronda);
                emp.setJugador1(j1);
                emp.setJugador2(j2);
                emp.setFechaHoraPartido(LocalDateTime.now().plusDays(ronda)); // Puedes ajustar la lógica de fecha/hora según tu necesidad

                emparejamientoRepository.save(emp);
                nuevosEmparejamientos.add(emp);

                System.out.println("generarEmparejamientos: Emparejamiento creado - Ronda " + ronda + ": Jugador " + j1.getNombreUsuario() + " vs " + j2.getNombreUsuario());
            }

            // Manejar BYE
            if (jugadorConBye != null) {
                Emparejamiento empBye = new Emparejamiento();
                empBye.setTorneo(torneo);
                empBye.setNumeroRonda(ronda);
                empBye.setJugador1(jugadorConBye);
                empBye.setJugador2(null);
                empBye.setFechaHoraPartido(LocalDateTime.now().plusDays(ronda));

                emparejamientoRepository.save(empBye);
                nuevosEmparejamientos.add(empBye);

                // Resultado automático para el BYE
                Resultado resultadoBye = new Resultado();
                resultadoBye.setEmparejamiento(empBye);
                resultadoBye.setGanador(jugadorConBye);
                resultadoBye.setSet1Jugador1Score(0);
                resultadoBye.setSet1Jugador2Score(0);
                resultadoBye.setSet2Jugador1Score(0);
                resultadoBye.setSet2Jugador2Score(0);
                resultadoBye.setSet3Jugador1Score(0);
                resultadoBye.setSet3Jugador2Score(0);
                resultadoBye.setJuegosGanadosJugador1(0);
                resultadoBye.setJuegosGanadosJugador2(0);
                resultadoRepository.save(resultadoBye);

                System.out.println("generarEmparejamientos: Resultado automático registrado para el BYE del jugador " + jugadorConBye.getNombreUsuario());

            }

            System.out.println("generarEmparejamientos: Generación de emparejamientos para la ronda " + ronda + " completada.");
            return ResponseEntity.ok("Emparejamientos generados exitosamente para la ronda " + ronda + ".");
        }
    }

    public ResponseEntity<String> calcularGanador(Long emparejamientoId) {
        return resultadoService.calcularGanadorPartido(emparejamientoId);
    }


    @Transactional
    public void verificarYGestionarSiguienteRonda(Long torneoId) {
        List<Emparejamiento> emparejamientos = emparejamientoRepository.findByTorneo_Id(torneoId);

        if (emparejamientos.isEmpty()) {
            System.out.println("No hay emparejamientos en este torneo.");
            return;
        }

        // Obtener la última ronda
        int ultimaRonda = emparejamientos.stream()
                .mapToInt(Emparejamiento::getNumeroRonda)
                .max()
                .orElse(0);

        // Verificar si todos los emparejamientos de la última ronda tienen resultados
        List<Emparejamiento> emparejamientosUltimaRonda = emparejamientos.stream()
                .filter(emp -> emp.getNumeroRonda() == ultimaRonda)
                .collect(Collectors.toList());

        boolean todosTienenResultados = emparejamientosUltimaRonda.stream()
                .allMatch(emp -> !emp.getResultados().isEmpty());

        if (todosTienenResultados) {
            System.out.println("Todos los emparejamientos de la ronda " + ultimaRonda + " tienen resultados. Generando la siguiente ronda.");
            generarEmparejamientos(torneoId, ultimaRonda + 1);
        } else {
            System.out.println("Aún faltan resultados en la ronda " + ultimaRonda + ". No se puede generar la siguiente ronda.");
        }
    }


    }
