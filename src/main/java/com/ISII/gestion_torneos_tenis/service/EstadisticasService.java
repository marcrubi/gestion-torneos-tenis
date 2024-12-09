package com.ISII.gestion_torneos_tenis.service;

import com.ISII.gestion_torneos_tenis.model.Jugador;
import com.ISII.gestion_torneos_tenis.model.Resultado;
import com.ISII.gestion_torneos_tenis.model.Emparejamiento;
import com.ISII.gestion_torneos_tenis.repository.ResultadoRepository;
import com.ISII.gestion_torneos_tenis.repository.JugadorRepository;
import com.ISII.gestion_torneos_tenis.repository.EmparejamientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EstadisticasService {

    @Autowired
    private ResultadoRepository resultadoRepository;

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private EmparejamientoRepository emparejamientoRepository;

    public static class EstadisticasJugador {
        private String nombreUsuario;
        private String nombre;
        private String apellidos;
        private int partidosGanados;
        private int setsGanados;
        private int setsPerdidos;
        private int juegosGanados;
        private int juegosPerdidos;
        private int posicionRanking;

        // Getters y Setters

        public String getNombreUsuario() { return nombreUsuario; }
        public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getApellidos() { return apellidos; }
        public void setApellidos(String apellidos) { this.apellidos = apellidos; }

        public int getPartidosGanados() { return partidosGanados; }
        public void setPartidosGanados(int partidosGanados) { this.partidosGanados = partidosGanados; }

        public int getSetsGanados() { return setsGanados; }
        public void setSetsGanados(int setsGanados) { this.setsGanados = setsGanados; }

        public int getSetsPerdidos() { return setsPerdidos; }
        public void setSetsPerdidos(int setsPerdidos) { this.setsPerdidos = setsPerdidos; }

        public int getJuegosGanados() { return juegosGanados; }
        public void setJuegosGanados(int juegosGanados) { this.juegosGanados = juegosGanados; }

        public int getJuegosPerdidos() { return juegosPerdidos; }
        public void setJuegosPerdidos(int juegosPerdidos) { this.juegosPerdidos = juegosPerdidos; }

        public int getPosicionRanking() { return posicionRanking; }
        public void setPosicionRanking(int posicionRanking) { this.posicionRanking = posicionRanking; }
    }

    @Transactional(readOnly = true)
    public EstadisticasJugador calcularEstadisticas(Long jugadorId) {
        Jugador jugador = jugadorRepository.findById(jugadorId)
                .orElseThrow(() -> new IllegalArgumentException("Jugador no encontrado con ID: " + jugadorId));

        EstadisticasJugador estadisticas = new EstadisticasJugador();
        estadisticas.setNombreUsuario(jugador.getNombreUsuario());
        estadisticas.setNombre(jugador.getNombre());
        estadisticas.setApellidos(jugador.getApellidos());
        estadisticas.setPartidosGanados(0);
        estadisticas.setSetsGanados(0);
        estadisticas.setSetsPerdidos(0);
        estadisticas.setJuegosGanados(0);
        estadisticas.setJuegosPerdidos(0);

        // Obtener todos los emparejamientos donde participó el jugador
        List<Emparejamiento> emparejamientosJugador = emparejamientoRepository.findAll().stream()
                .filter(e -> ((e.getJugador1() != null && e.getJugador1().getId().equals(jugadorId))
                        || (e.getJugador2() != null && e.getJugador2().getId().equals(jugadorId))))
                .toList();

        for (Emparejamiento e : emparejamientosJugador) {
            int setsGanadosPartido = 0;
            int setsPerdidosPartido = 0;

            for (Resultado r : e.getResultados()) {
                // Determinar si el jugador es J1 o J2 en este emparejamiento
                boolean esJugador1 = (e.getJugador1() != null && e.getJugador1().getId().equals(jugadorId));

                // Set1
                Integer s1j1 = r.getSet1Jugador1Score();
                Integer s1j2 = r.getSet1Jugador2Score();
                if (s1j1 != null && s1j2 != null) {
                    if (esJugador1) {
                        if (s1j1 > s1j2) {
                            estadisticas.setSetsGanados(estadisticas.getSetsGanados() + 1);
                            setsGanadosPartido++;
                        } else {
                            estadisticas.setSetsPerdidos(estadisticas.getSetsPerdidos() + 1);
                            setsPerdidosPartido++;
                        }
                        estadisticas.setJuegosGanados(estadisticas.getJuegosGanados() + s1j1);
                        estadisticas.setJuegosPerdidos(estadisticas.getJuegosPerdidos() + s1j2);
                    } else {
                        if (s1j2 > s1j1) {
                            estadisticas.setSetsGanados(estadisticas.getSetsGanados() + 1);
                            setsGanadosPartido++;
                        } else {
                            estadisticas.setSetsPerdidos(estadisticas.getSetsPerdidos() + 1);
                            setsPerdidosPartido++;
                        }
                        estadisticas.setJuegosGanados(estadisticas.getJuegosGanados() + s1j2);
                        estadisticas.setJuegosPerdidos(estadisticas.getJuegosPerdidos() + s1j1);
                    }
                }

                // Set2
                Integer s2j1 = r.getSet2Jugador1Score();
                Integer s2j2 = r.getSet2Jugador2Score();
                if (s2j1 != null && s2j2 != null) {
                    if (esJugador1) {
                        if (s2j1 > s2j2) {
                            estadisticas.setSetsGanados(estadisticas.getSetsGanados() + 1);
                            setsGanadosPartido++;
                        } else {
                            estadisticas.setSetsPerdidos(estadisticas.getSetsPerdidos() + 1);
                            setsPerdidosPartido++;
                        }
                        estadisticas.setJuegosGanados(estadisticas.getJuegosGanados() + s2j1);
                        estadisticas.setJuegosPerdidos(estadisticas.getJuegosPerdidos() + s2j2);
                    } else {
                        if (s2j2 > s2j1) {
                            estadisticas.setSetsGanados(estadisticas.getSetsGanados() + 1);
                            setsGanadosPartido++;
                        } else {
                            estadisticas.setSetsPerdidos(estadisticas.getSetsPerdidos() + 1);
                            setsPerdidosPartido++;
                        }
                        estadisticas.setJuegosGanados(estadisticas.getJuegosGanados() + s2j2);
                        estadisticas.setJuegosPerdidos(estadisticas.getJuegosPerdidos() + s2j1);
                    }
                }

                // Set3
                Integer s3j1 = r.getSet3Jugador1Score();
                Integer s3j2 = r.getSet3Jugador2Score();
                if (s3j1 != null && s3j2 != null) {
                    if (esJugador1) {
                        if (s3j1 > s3j2) {
                            estadisticas.setSetsGanados(estadisticas.getSetsGanados() + 1);
                            setsGanadosPartido++;
                        } else {
                            estadisticas.setSetsPerdidos(estadisticas.getSetsPerdidos() + 1);
                            setsPerdidosPartido++;
                        }
                        estadisticas.setJuegosGanados(estadisticas.getJuegosGanados() + s3j1);
                        estadisticas.setJuegosPerdidos(estadisticas.getJuegosPerdidos() + s3j2);
                    } else {
                        if (s3j2 > s3j1) {
                            estadisticas.setSetsGanados(estadisticas.getSetsGanados() + 1);
                            setsGanadosPartido++;
                        } else {
                            estadisticas.setSetsPerdidos(estadisticas.getSetsPerdidos() + 1);
                            setsPerdidosPartido++;
                        }
                        estadisticas.setJuegosGanados(estadisticas.getJuegosGanados() + s3j2);
                        estadisticas.setJuegosPerdidos(estadisticas.getJuegosPerdidos() + s3j1);
                    }
                }
            }

            // Determinar si el jugador ganó el partido
            if (setsGanadosPartido > setsPerdidosPartido) {
                estadisticas.setPartidosGanados(estadisticas.getPartidosGanados() + 1);
            }
        }

        return estadisticas;
    }
}
