// src/main/java/com/ISII/gestion_torneos_tenis/model/Inscripcion.java

package com.ISII.gestion_torneos_tenis.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inscripciones")
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;

    @ManyToOne
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;

    @JoinColumn(name = "posicion_final")
    private Integer posicionFinal;

    private String estadoInscripcion;

    private LocalDateTime fechaInscripcion;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    public Inscripcion() {
    }

    public Inscripcion(Jugador jugador, Torneo torneo, String estadoInscripcion, LocalDateTime fechaInscripcion) {
        this.jugador = jugador;
        this.torneo = torneo;
        this.estadoInscripcion = estadoInscripcion;
        this.fechaInscripcion = fechaInscripcion;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }

    public Torneo getTorneo() {
        return torneo;
    }

    public void setTorneo(Torneo torneo) {
        this.torneo = torneo;
    }

    public String getEstadoInscripcion() {
        return estadoInscripcion;
    }

    public void setEstadoInscripcion(String estadoInscripcion) {
        this.estadoInscripcion = estadoInscripcion;
    }

    public LocalDateTime getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(LocalDateTime fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public Integer getPosicionFinal() {
        return posicionFinal;
    }
    public void setPosicionFinal(Integer posicionFinal) {
        this.posicionFinal = posicionFinal;
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
