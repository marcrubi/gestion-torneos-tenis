// src/main/java/com/ISII/gestion_torneos_tenis/dto/TorneoConEmparejamientosDTO.java

package com.ISII.gestion_torneos_tenis.dto;

import com.ISII.gestion_torneos_tenis.model.Torneo;

import java.util.List;

public class TorneoConEmparejamientosDTO {
    private Torneo torneo;
    private String estadoInscripcion;
    private int posicionRanking;
    private List<EmparejamientoDTO> emparejamientos;

    // Constructor
    public TorneoConEmparejamientosDTO(Torneo torneo, String estadoInscripcion, int posicionRanking, List<EmparejamientoDTO> emparejamientos) {
        this.torneo = torneo;
        this.estadoInscripcion = estadoInscripcion;
        this.posicionRanking = posicionRanking;
        this.emparejamientos = emparejamientos;
    }

    // Getters y Setters
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

    public int getPosicionRanking() {
        return posicionRanking;
    }

    public void setPosicionRanking(int posicionRanking) {
        this.posicionRanking = posicionRanking;
    }

    public List<EmparejamientoDTO> getEmparejamientos() {
        return emparejamientos;
    }

    public void setEmparejamientos(List<EmparejamientoDTO> emparejamientos) {
        this.emparejamientos = emparejamientos;
    }
}
