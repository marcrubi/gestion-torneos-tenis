// src/main/java/com/ISII/gestion_torneos_tenis/service/TorneoService.java

package com.ISII.gestion_torneos_tenis.service;

import com.ISII.gestion_torneos_tenis.model.*;
import com.ISII.gestion_torneos_tenis.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.http.HttpStatus;


import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
public class TorneoService {

    @Autowired
    private TorneoRepository torneoRepository;

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private EmparejamientoRepository emparejamientoRepository;

    @Autowired
    private EstadisticasService estadisticasService; // Necesitamos para cálculo de stats

    @Autowired
    private JugadorService jugadorService;

    @Autowired
    private RankingService rankingService;

    /**
     * Obtiene un torneo por su ID.
     */
    public Torneo getTorneoById(Long torneoId) {
        return torneoRepository.findById(torneoId)
                .orElseThrow(() -> new RuntimeException("Torneo no encontrado con ID: " + torneoId));
    }

    /**
     * Obtiene los torneos disponibles con estado "Pendiente".
     */
    public List<Torneo> obtenerTorneosDisponibles() {
        return torneoRepository.findByEstadoTorneo("Pendiente");
    }

    /**
     * Método placeholder para generar emparejamientos.
     */
    public ResponseEntity<String> generarEmparejamientos(Long torneoId) {
        return new ResponseEntity<>("Función no utilizada aquí", HttpStatus.OK);
    }

    /**
     * Obtiene el ID del torneo al que pertenece un emparejamiento.
     */
    public Long obtenerTorneoPorEmparejamiento(Long emparejamientoId) {
        Optional<Emparejamiento> emparejamientoOpt = emparejamientoRepository.findById(emparejamientoId);
        if (emparejamientoOpt.isEmpty()) {
            throw new RuntimeException("Emparejamiento no encontrado con ID: " + emparejamientoId);
        }
        return emparejamientoOpt.get().getTorneo().getId();
    }

    /**
     * Obtiene todos los torneos.
     */
    public List<Torneo> obtenerTodosLosTorneos() {
        return torneoRepository.findAll();
    }

    /**
     * Actualiza los puntos de un jugador.
     */
    public void actualizarPuntosGanador(Long jugadorId, int puntosAsignados) {
        Jugador jugador = jugadorRepository.findById(jugadorId)
                .orElseThrow(() -> new UsernameNotFoundException("Jugador no encontrado con ID: " + jugadorId));
        jugador.setPuntos(jugador.getPuntos() + puntosAsignados);
        jugadorRepository.save(jugador);
    }

    /**
     * Obtiene los torneos disponibles para inscripción.
     */
    public List<Torneo> obtenerTorneosDisponiblesParaInscripcion() {
        List<Torneo> todosTorneos = torneoRepository.findAll();
        return todosTorneos.stream()
                .filter(torneo -> "Inscrito".equalsIgnoreCase(torneo.getEstadoTorneo()))
                .filter(torneo -> LocalDateTime.now().isBefore(torneo.getFechaLimiteInscripcion()))
                .collect(Collectors.toList());
    }

    /**
     * Método para finalizar el torneo:
     * - Calcular la clasificación final de todos los jugadores confirmados.
     * - Asignar puntos según posición final.
     * - Marcar torneo como Finalizado.
     */
    public void finalizarTorneo(Long torneoId) {
        Torneo torneo = getTorneoById(torneoId);

        // Obtener jugadores confirmados
        List<Inscripcion> inscripcionesConfirmadas = inscripcionRepository.findByTorneo(torneo).stream()
                .filter(ins -> "Confirmada".equalsIgnoreCase(ins.getEstadoInscripcion()))
                .collect(Collectors.toList());

        if (inscripcionesConfirmadas.isEmpty()) {
            // Sin jugadores confirmados, no hay nada que finalizar
            torneo.setEstadoTorneo("Finalizado");
            torneoRepository.save(torneo);
            return;
        }

        List<Jugador> jugadores = inscripcionesConfirmadas.stream()
                .map(Inscripcion::getJugador)
                .collect(Collectors.toList());

        // Calcular clasificación final
        List<JugadorClasificado> clasificacion = calcularClasificacionFinal(torneoId, jugadores);

        // Asignar puntos finales
        for (int i = 0; i < clasificacion.size(); i++) {
            JugadorClasificado jc = clasificacion.get(i);
            int posicion = i + 1;
            int puntos = calcularPuntosPorPosicion(posicion, clasificacion.size());
            // Actualizar puntos del jugador
            Jugador j = jc.jugador;
            j.setPuntos(j.getPuntos() + puntos);
            jugadorRepository.save(j);
            Inscripcion inscripcion = inscripcionRepository.findByJugadorAndTorneo(j, torneo)
                    .orElseThrow(() -> new RuntimeException("Inscripción no encontrada para jugador y torneo."));
            inscripcion.setPosicionFinal(posicion);
            inscripcionRepository.save(inscripcion);
        }

        // Marcar el torneo como finalizado
        torneo.setEstadoTorneo("Finalizado");
        torneoRepository.save(torneo);
    }

    /**
     * Estructura auxiliar para almacenar datos de clasificación.
     */
    private static class JugadorClasificado {
        Jugador jugador;
        int rondaAlcanzada;
        int setsGanados;
        int setsPerdidos;
        int juegosGanados;
        int juegosPerdidos;
    }

    /**
     * Calcula la clasificación final de los jugadores aplicando los criterios:
     * 1. Ronda alcanzada (mayor es mejor)
     * 2. En caso de empate en ronda: mayor sets ganados
     * 3. Luego mayor juegos ganados
     * 4. Luego menor juegos perdidos
     * 5. Si persiste el empate, desempate aleatorio
     */
    private List<JugadorClasificado> calcularClasificacionFinal(Long torneoId, List<Jugador> jugadores) {
        Torneo torneo = getTorneoById(torneoId);

        // Obtener todos los emparejamientos del torneo
        List<Emparejamiento> emparejamientos = emparejamientoRepository.findByTorneo(torneo);
        int maxRonda = emparejamientos.stream().mapToInt(Emparejamiento::getNumeroRonda).max().orElse(1);

        Random random = new Random();

        List<JugadorClasificado> lista = new ArrayList<>();
        for (Jugador j : jugadores) {
            JugadorClasificado jc = new JugadorClasificado();
            jc.jugador = j;
            jc.rondaAlcanzada = rondaAlcanzadaDelJugador(torneo, j);

            // Calcular estadísticas por torneo del jugador (setsGanados, setsPerdidos, juegosGanados, juegosPerdidos)
            EstadisticasService.EstadisticasJugador est = calcularEstadisticasPorTorneo(torneoId, j.getId());
            jc.setsGanados = est.getSetsGanados();
            jc.setsPerdidos = est.getSetsPerdidos();
            jc.juegosGanados = est.getJuegosGanados();
            jc.juegosPerdidos = est.getJuegosPerdidos();

            lista.add(jc);
        }

        // Ordenar la lista según los criterios:
        // Primero por rondaAlcanzada DESC (más ronda = mejor)
        // luego por setsGanados DESC
        // luego por juegosGanados DESC
        // luego por juegosPerdidos ASC
        // si persiste empate, orden aleatorio
        lista.sort((a, b) -> {
            int cmp = Integer.compare(b.rondaAlcanzada, a.rondaAlcanzada);
            if (cmp != 0) return cmp;
            cmp = Integer.compare(b.setsGanados, a.setsGanados);
            if (cmp != 0) return cmp;
            cmp = Integer.compare(b.juegosGanados, a.juegosGanados);
            if (cmp != 0) return cmp;
            cmp = Integer.compare(a.juegosPerdidos, b.juegosPerdidos);
            if (cmp != 0) return cmp;
            // Empate total, desempate aleatorio
            return random.nextBoolean() ? 1 : -1;
        });

        return lista;
    }

    /**
     * Determina la ronda alcanzada por un jugador.
     */
    private int rondaAlcanzadaDelJugador(Torneo torneo, Jugador jugador) {
        List<Emparejamiento> emparejamientos = emparejamientoRepository.findByTorneo(torneo);
        if (emparejamientos.isEmpty()) {
            return 0;
        }

        return emparejamientos.stream()
                .filter(e -> ((e.getJugador1() != null && e.getJugador1().equals(jugador)) || (e.getJugador2() != null && e.getJugador2().equals(jugador))))
                .mapToInt(Emparejamiento::getNumeroRonda)
                .max().orElse(0);
    }

    /**
     * Calcula las estadísticas por torneo y jugador (sets/juegos ganados/perdidos, etc.).
     */
    private EstadisticasService.EstadisticasJugador calcularEstadisticasPorTorneo(Long torneoId, Long jugadorId) {
        Torneo torneo = getTorneoById(torneoId);
        List<Emparejamiento> emparejamientosTorneo = emparejamientoRepository.findByTorneo(torneo).stream()
                .filter(e -> ((e.getJugador1() != null && e.getJugador1().getId().equals(jugadorId))
                        || (e.getJugador2() != null && e.getJugador2().getId().equals(jugadorId))))
                .collect(Collectors.toList());

        EstadisticasService.EstadisticasJugador est = new EstadisticasService.EstadisticasJugador();
        est.setNombreUsuario(jugadorRepository.findById(jugadorId).orElseThrow().getNombreUsuario());
        est.setNombre(jugadorRepository.findById(jugadorId).orElseThrow().getNombre());
        est.setApellidos(jugadorRepository.findById(jugadorId).orElseThrow().getApellidos());
        est.setPartidosGanados(0);
        est.setSetsGanados(0);
        est.setSetsPerdidos(0);
        est.setJuegosGanados(0);
        est.setJuegosPerdidos(0);

        for (Emparejamiento e : emparejamientosTorneo) {
            int setsGanadosPartido = 0;
            int setsPerdidosPartido = 0;
            boolean esJugador1 = (e.getJugador1() != null && e.getJugador1().getId().equals(jugadorId));

            for (Resultado r : e.getResultados()) {
                // Set1
                Integer s1j1 = r.getSet1Jugador1Score();
                Integer s1j2 = r.getSet1Jugador2Score();
                if (s1j1 != null && s1j2 != null) {
                    if (esJugador1) {
                        if (s1j1 > s1j2) {
                            est.setSetsGanados(est.getSetsGanados() + 1);
                            setsGanadosPartido++;
                        } else {
                            est.setSetsPerdidos(est.getSetsPerdidos() + 1);
                            setsPerdidosPartido++;
                        }
                        est.setJuegosGanados(est.getJuegosGanados() + s1j1);
                        est.setJuegosPerdidos(est.getJuegosPerdidos() + s1j2);
                    } else {
                        if (s1j2 > s1j1) {
                            est.setSetsGanados(est.getSetsGanados() + 1);
                            setsGanadosPartido++;
                        } else {
                            est.setSetsPerdidos(est.getSetsPerdidos() + 1);
                            setsPerdidosPartido++;
                        }
                        est.setJuegosGanados(est.getJuegosGanados() + s1j2);
                        est.setJuegosPerdidos(est.getJuegosPerdidos() + s1j1);
                    }
                }

                // Set2
                Integer s2j1 = r.getSet2Jugador1Score();
                Integer s2j2 = r.getSet2Jugador2Score();
                if (s2j1 != null && s2j2 != null) {
                    if (esJugador1) {
                        if (s2j1 > s2j2) {
                            est.setSetsGanados(est.getSetsGanados() + 1);
                            setsGanadosPartido++;
                        } else {
                            est.setSetsPerdidos(est.getSetsPerdidos() + 1);
                            setsPerdidosPartido++;
                        }
                        est.setJuegosGanados(est.getJuegosGanados() + s2j1);
                        est.setJuegosPerdidos(est.getJuegosPerdidos() + s2j2);
                    } else {
                        if (s2j2 > s2j1) {
                            est.setSetsGanados(est.getSetsGanados() + 1);
                            setsGanadosPartido++;
                        } else {
                            est.setSetsPerdidos(est.getSetsPerdidos() + 1);
                            setsPerdidosPartido++;
                        }
                        est.setJuegosGanados(est.getJuegosGanados() + s2j2);
                        est.setJuegosPerdidos(est.getJuegosPerdidos() + s2j1);
                    }
                }

                // Set3
                Integer s3j1 = r.getSet3Jugador1Score();
                Integer s3j2 = r.getSet3Jugador2Score();
                if (s3j1 != null && s3j2 != null) {
                    if (esJugador1) {
                        if (s3j1 > s3j2) {
                            est.setSetsGanados(est.getSetsGanados() + 1);
                            setsGanadosPartido++;
                        } else {
                            est.setSetsPerdidos(est.getSetsPerdidos() + 1);
                            setsPerdidosPartido++;
                        }
                        est.setJuegosGanados(est.getJuegosGanados() + s3j1);
                        est.setJuegosPerdidos(est.getJuegosPerdidos() + s3j2);
                    } else {
                        if (s3j2 > s3j1) {
                            est.setSetsGanados(est.getSetsGanados() + 1);
                            setsGanadosPartido++;
                        } else {
                            est.setSetsPerdidos(est.getSetsPerdidos() + 1);
                            setsPerdidosPartido++;
                        }
                        est.setJuegosGanados(est.getJuegosGanados() + s3j2);
                        est.setJuegosPerdidos(est.getJuegosPerdidos() + s3j1);
                    }
                }
            }

            // Determinar si ganó el partido
            if (setsGanadosPartido > setsPerdidosPartido) {
                est.setPartidosGanados(est.getPartidosGanados() + 1);
            }
        }

        return est;
    }

    /**
     * Método para obtener la posición final de un jugador en un torneo.
     */
    public int obtenerPosicionFinal(Torneo torneo, Jugador jugador) {
        // Obtener todos los emparejamientos del torneo
        List<Emparejamiento> emparejamientos = emparejamientoRepository.findByTorneo(torneo);
        if (emparejamientos.isEmpty()) {
            return 0; // Sin emparejamientos, sin posición
        }

        // Calcular la clasificación final
        List<JugadorClasificado> clasificacion = calcularClasificacionFinal(torneo.getId(),
                inscripcionRepository.findByTorneo(torneo).stream()
                        .map(Inscripcion::getJugador)
                        .collect(Collectors.toList())
        );

        // Buscar la posición del jugador
        for (int i = 0; i < clasificacion.size(); i++) {
            if (clasificacion.get(i).jugador.equals(jugador)) {
                return i + 1; // Posición es índice +1
            }
        }

        return 0; // No encontrado
    }

    /**
     * Determina si el torneo está finalizado:
     * - Si todos los emparejamientos de la última ronda tienen resultados,
     *   entonces finaliza el torneo.
     */
    public void chequearYFinalizarTorneoSiCorresponde(Long torneoId) {
        Torneo torneo = getTorneoById(torneoId);

        // Obtener todos los emparejamientos del torneo
        List<Emparejamiento> emparejamientos = emparejamientoRepository.findByTorneo(torneo);

        if (emparejamientos.isEmpty()) {
            // Sin emparejamientos, no se puede finalizar el torneo
            System.out.println("chequearYFinalizarTorneoSiCorresponde: No hay emparejamientos para el torneo ID " + torneoId);
            return;
        }

        // Encontrar la máxima ronda del torneo
        int maxRonda = emparejamientos.stream()
                .mapToInt(Emparejamiento::getNumeroRonda)
                .max()
                .orElse(1);

        // Obtener emparejamientos de la última ronda
        List<Emparejamiento> ultimaRonda = emparejamientos.stream()
                .filter(e -> e.getNumeroRonda() == maxRonda)
                .collect(Collectors.toList());

        if (ultimaRonda.isEmpty()) {
            // Si no hay emparejamientos en la última ronda, no se puede finalizar
            System.out.println("chequearYFinalizarTorneoSiCorresponde: No hay emparejamientos en la última ronda del torneo ID " + torneoId);
            return;
        }

        // Verificar si todos los emparejamientos de la última ronda tienen resultados
        boolean todosConResultados = ultimaRonda.stream()
                .allMatch(e -> e.getResultados() != null && !e.getResultados().isEmpty());

        if (!todosConResultados) {
            // Aún hay emparejamientos sin resultados
            System.out.println("chequearYFinalizarTorneoSiCorresponde: Aún hay emparejamientos sin resultados en la última ronda del torneo ID " + torneoId);
            return;
        }

        // Si todos los emparejamientos de la última ronda tienen resultados, finalizar el torneo
        System.out.println("chequearYFinalizarTorneoSiCorresponde: Finalizando el torneo ID " + torneoId);
        finalizarTorneo(torneoId);
    }


    /**
     * Asigna puntos según la posición final (1 a 16) según el enunciado:
     * 1º: 2000
     * 2º: 1500
     * 3º: 1000
     * 4º: 500
     * 5º: 475
     * 6º: 450
     * 7º: 425
     * 8º: 400
     * 9º: 375
     * 10º:350
     * 11º:325
     * 12º:300
     * 13º:275
     * 14º:250
     * 15º:225
     * 16º:200
     * Si el torneo tiene menos jugadores, solo asignar hasta esa posición.
     */
    private int calcularPuntosPorPosicion(int posicion, int totalJugadores) {
        // Si la posición es mayor al número de jugadores, asignar 0 puntos
        if (posicion > totalJugadores) return 0;

        switch (posicion) {
            case 1: return 2000;
            case 2: return 1500;
            case 3: return 1000;
            case 4: return 500;
            case 5: return 475;
            case 6: return 450;
            case 7: return 425;
            case 8: return 400;
            case 9: return 375;
            case 10:return 350;
            case 11:return 325;
            case 12:return 300;
            case 13:return 275;
            case 14:return 250;
            case 15:return 225;
            case 16:return 200;
            default:
                // Para posiciones mayores a 16, asignar 200 puntos
                return 200;
        }
    }

    /**
     * Método para obtener los emparejamientos de un torneo.
     */
    public int contarTorneosDisponibles(){
        return torneoRepository.findByEstadoTorneo("Pendiente").size();
    }

}
