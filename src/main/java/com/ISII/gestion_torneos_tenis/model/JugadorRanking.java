//// src/main/java/com/ISII/gestion_torneos_tenis/model/JugadorRanking.java
//
//package com.ISII.gestion_torneos_tenis.model;
//
//import jakarta.persistence.*;
//import jakarta.validation.constraints.NotNull;
//
//@Entity
//@Table(name = "jugador_ranking")
//public class JugadorRanking {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @OneToOne
//    @JoinColumn(name = "jugador_id", nullable = false)
//    private Jugador jugador;
//
//    @NotNull
//    private int puntos;
//
//    public JugadorRanking() {
//    }
//
//    public JugadorRanking(Jugador jugador, int puntos) {
//        this.jugador = jugador;
//        this.puntos = puntos;
//    }
//
//    // Getters y Setters
//
//    public Long getId() {
//        return id;
//    }
//
//    public Jugador getJugador() {
//        return jugador;
//    }
//
//    public void setJugador(Jugador jugador) {
//        this.jugador = jugador;
//    }
//
//    public int getPuntos() {
//        return puntos;
//    }
//
//    public void setPuntos(int puntos) {
//        this.puntos = puntos;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//}
