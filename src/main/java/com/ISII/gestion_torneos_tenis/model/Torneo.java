package com.ISII.gestion_torneos_tenis.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "torneos")
public class Torneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del torneo es obligatorio.")
    private String nombreTorneo;

    private LocalDateTime fechaLimiteInscripcion;

    @Column(name = "estado_torneo")
    private String estadoTorneo;

    private int numeroMaxJugadores;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    private int puntosAsignados;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "torneo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inscripcion> inscripciones;

    @OneToMany(mappedBy = "torneo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Emparejamiento> emparejamientos;

    public Torneo() {
    }

    public Torneo(String nombreTorneo, LocalDateTime fechaLimiteInscripcion, String estadoTorneo, int numeroMaxJugadores, LocalDateTime fechaInicio, LocalDateTime fechaFin, int puntosAsignados) {
        this.nombreTorneo = nombreTorneo;
        this.fechaLimiteInscripcion = fechaLimiteInscripcion;
        this.estadoTorneo = estadoTorneo;
        this.numeroMaxJugadores = numeroMaxJugadores;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.puntosAsignados = puntosAsignados;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public String getNombreTorneo() {
        return nombreTorneo;
    }

    public void setNombreTorneo(String nombreTorneo) {
        this.nombreTorneo = nombreTorneo;
    }

    public LocalDateTime getFechaLimiteInscripcion() {
        return fechaLimiteInscripcion;
    }

    public void setFechaLimiteInscripcion(LocalDateTime fechaLimiteInscripcion) {
        this.fechaLimiteInscripcion = fechaLimiteInscripcion;
    }

    public String getEstadoTorneo() {
        return estadoTorneo;
    }

    public void setEstadoTorneo(String estadoTorneo) {
        this.estadoTorneo = estadoTorneo;
    }

    public int getNumeroMaxJugadores() {
        return numeroMaxJugadores;
    }

    public void setNumeroMaxJugadores(int numeroMaxJugadores) {
        this.numeroMaxJugadores = numeroMaxJugadores;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public int getPuntosAsignados() {
        return puntosAsignados;
    }

    public void setPuntosAsignados(int puntosAsignados) {
        this.puntosAsignados = puntosAsignados;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public List<Inscripcion> getInscripciones() {
        return inscripciones;
    }

    public void setInscripciones(List<Inscripcion> inscripciones) {
        this.inscripciones = inscripciones;
    }

    public List<Emparejamiento> getEmparejamientos() {
        return emparejamientos;
    }

    public void setEmparejamientos(List<Emparejamiento> emparejamientos) {
        this.emparejamientos = emparejamientos;
    }

    public Set<Jugador> getJugadores() {
        Set<Jugador> jugadores = new HashSet<>();
        if (inscripciones != null) {
            for (Inscripcion inscripcion : inscripciones) {
                jugadores.add(inscripcion.getJugador());
            }
        }
        return jugadores;
    }

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }


}
