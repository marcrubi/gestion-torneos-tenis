// src/main/java/com/ISII/gestion_torneos_tenis/model/Resultado.java

package com.ISII.gestion_torneos_tenis.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resultados")
public class Resultado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emparejamiento_id", nullable = false)
    private Emparejamiento emparejamiento;

    // Scores for up to 3 sets
    @Column(name = "set1_jugador1_score")
    private Integer set1Jugador1Score;

    @Column(name = "set1_jugador2_score")
    private Integer set1Jugador2Score;

    @Column(name = "set2_jugador1_score")
    private Integer set2Jugador1Score;

    @Column(name = "set2_jugador2_score")
    private Integer set2Jugador2Score;

    @Column(name = "set3_jugador1_score")
    private Integer set3Jugador1Score;

    @Column(name = "set3_jugador2_score")
    private Integer set3Jugador2Score;

    @Column(name = "set4_jugador1_score")
    private Integer set4Jugador1Score;

    @Column(name = "set4_jugador2_score")
    private Integer set4Jugador2Score;

    @Column(name = "set5_jugador1_score")
    private Integer set5Jugador1Score;

    @Column(name = "set5_jugador2_score")
    private Integer set5Jugador2Score;

    @ManyToOne
    @JoinColumn(name = "ganador_id", nullable = false)
    private Jugador ganador;

    // Nuevos campos para juegos ganados
    @Column(name = "juegos_ganados_jugador1", nullable = false)
    private Integer juegosGanadosJugador1;

    @Column(name = "juegos_ganados_jugador2", nullable = false)
    private Integer juegosGanadosJugador2;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    // Constructor por defecto
    public Resultado() {
    }

    public Resultado(Emparejamiento emparejamiento,
                     Integer set1Jugador1Score, Integer set1Jugador2Score,
                     Integer set2Jugador1Score, Integer set2Jugador2Score,
                     Integer set3Jugador1Score, Integer set3Jugador2Score,
                     Integer set4Jugador1Score, Integer set4Jugador2Score,
                     Integer set5Jugador1Score, Integer set5Jugador2Score,
                     Jugador ganador,
                     Integer juegosGanadosJugador1, Integer juegosGanadosJugador2) {
        this.emparejamiento = emparejamiento;
        this.set1Jugador1Score = set1Jugador1Score;
        this.set1Jugador2Score = set1Jugador2Score;
        this.set2Jugador1Score = set2Jugador1Score;
        this.set2Jugador2Score = set2Jugador2Score;
        this.set3Jugador1Score = set3Jugador1Score;
        this.set3Jugador2Score = set3Jugador2Score;
        this.set4Jugador1Score = set4Jugador1Score;
        this.set4Jugador2Score = set4Jugador2Score;
        this.set5Jugador1Score = set5Jugador1Score;
        this.set5Jugador2Score = set5Jugador2Score;
        this.ganador = ganador;
        this.juegosGanadosJugador1 = juegosGanadosJugador1;
        this.juegosGanadosJugador2 = juegosGanadosJugador2;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public Emparejamiento getEmparejamiento() {
        return emparejamiento;
    }

    public void setEmparejamiento(Emparejamiento emparejamiento) {
        this.emparejamiento = emparejamiento;
    }

    public Integer getSet1Jugador1Score() {
        return set1Jugador1Score;
    }

    public void setSet1Jugador1Score(Integer set1Jugador1Score) {
        this.set1Jugador1Score = set1Jugador1Score;
    }

    public Integer getSet1Jugador2Score() {
        return set1Jugador2Score;
    }

    public void setSet1Jugador2Score(Integer set1Jugador2Score) {
        this.set1Jugador2Score = set1Jugador2Score;
    }

    public Integer getSet2Jugador1Score() {
        return set2Jugador1Score;
    }

    public void setSet2Jugador1Score(Integer set2Jugador1Score) {
        this.set2Jugador1Score = set2Jugador1Score;
    }

    public Integer getSet2Jugador2Score() {
        return set2Jugador2Score;
    }

    public void setSet2Jugador2Score(Integer set2Jugador2Score) {
        this.set2Jugador2Score = set2Jugador2Score;
    }

    public Integer getSet3Jugador1Score() {
        return set3Jugador1Score;
    }

    public void setSet3Jugador1Score(Integer set3Jugador1Score) {
        this.set3Jugador1Score = set3Jugador1Score;
    }

    public Integer getSet3Jugador2Score() {
        return set3Jugador2Score;
    }

    public void setSet3Jugador2Score(Integer set3Jugador2Score) {
        this.set3Jugador2Score = set3Jugador2Score;
    }

    public Jugador getGanador() {
        return ganador;
    }

    public void setGanador(Jugador ganador) {
        this.ganador = ganador;
    }

    public Integer getJuegosGanadosJugador1() {
        return juegosGanadosJugador1;
    }

    public Integer getSet4Jugador1Score() {
        return set4Jugador1Score;
    }

    public void setSet4Jugador1Score(Integer set4Jugador1Score) {
        this.set4Jugador1Score = set4Jugador1Score;
    }

    public Integer getSet4Jugador2Score() {
        return set4Jugador2Score;
    }

    public void setSet4Jugador2Score(Integer set4Jugador2Score) {
        this.set4Jugador2Score = set4Jugador2Score;
    }

    public Integer getSet5Jugador1Score() {
        return set5Jugador1Score;
    }

    public void setSet5Jugador1Score(Integer set5Jugador1Score) {
        this.set5Jugador1Score = set5Jugador1Score;
    }

    public Integer getSet5Jugador2Score() {
        return set5Jugador2Score;
    }

    public void setSet5Jugador2Score(Integer set5Jugador2Score) {
        this.set5Jugador2Score = set5Jugador2Score;
    }

    public void setJuegosGanadosJugador1(Integer juegosGanadosJugador1) {
        this.juegosGanadosJugador1 = juegosGanadosJugador1;
    }

    public Integer getJuegosGanadosJugador2() {
        return juegosGanadosJugador2;
    }

    public void setJuegosGanadosJugador2(Integer juegosGanadosJugador2) {
        this.juegosGanadosJugador2 = juegosGanadosJugador2;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
