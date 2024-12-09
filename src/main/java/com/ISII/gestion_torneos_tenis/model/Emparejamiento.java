// src/main/java/com/ISII/gestion_torneos_tenis/model/Emparejamiento.java

package com.ISII.gestion_torneos_tenis.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "emparejamientos")
public class Emparejamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "torneo_id", nullable = false)
    private Torneo torneo;

    @NotNull
    private int numeroRonda;

    @ManyToOne
    @JoinColumn(name = "jugador1_id", nullable = false)
    private Jugador jugador1;

    @ManyToOne
    @JoinColumn(name = "jugador2_id")
    private Jugador jugador2;

    private LocalDateTime fechaHoraPartido;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "emparejamiento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Resultado> resultados = new ArrayList<>();


    public Emparejamiento() {
    }

    public Emparejamiento(Torneo torneo, int numeroRonda, Jugador jugador1, Jugador jugador2, LocalDateTime fechaHoraPartido ) {
        this.torneo = torneo;
        this.numeroRonda = numeroRonda;
        this.jugador1 = jugador1;
        this.jugador2 = jugador2;
        this.fechaHoraPartido = fechaHoraPartido;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public Torneo getTorneo() {
        return torneo;
    }

    public void setTorneo(Torneo torneo) {
        this.torneo = torneo;
    }

    public int getNumeroRonda() {
        return numeroRonda;
    }

    public void setNumeroRonda(int numeroRonda) {
        this.numeroRonda = numeroRonda;
    }

    public Jugador getJugador1() {
        return jugador1;
    }

    public void setJugador1(Jugador jugador1) {
        this.jugador1 = jugador1;
    }

    public Jugador getJugador2() {
        return jugador2;
    }

    public void setJugador2(Jugador jugador2) {
        this.jugador2 = jugador2;
    }

    public LocalDateTime getFechaHoraPartido() {
        return fechaHoraPartido;
    }

    public void setFechaHoraPartido(LocalDateTime fechaHoraPartido) {
        this.fechaHoraPartido = fechaHoraPartido;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public List<Resultado> getResultados() {
        return resultados;
    }

    public void setResultados(List<Resultado> resultados) {
        this.resultados = resultados;
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
