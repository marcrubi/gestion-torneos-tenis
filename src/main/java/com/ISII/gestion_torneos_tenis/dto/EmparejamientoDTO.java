// src/main/java/com/ISII/gestion_torneos_tenis/dto/EmparejamientoDTO.java

package com.ISII.gestion_torneos_tenis.dto;

import com.ISII.gestion_torneos_tenis.model.Emparejamiento;

public class EmparejamientoDTO {
    private Emparejamiento emparejamiento;
    private int posicionRankingJugador1;
    private int posicionRankingJugador2;

    // Constructor
    public EmparejamientoDTO(Emparejamiento emparejamiento, int posicionRankingJugador1, int posicionRankingJugador2) {
        this.emparejamiento = emparejamiento;
        this.posicionRankingJugador1 = posicionRankingJugador1;
        this.posicionRankingJugador2 = posicionRankingJugador2;
    }

    // Getters y Setters
    public Emparejamiento getEmparejamiento() {
        return emparejamiento;
    }

    public void setEmparejamiento(Emparejamiento emparejamiento) {
        this.emparejamiento = emparejamiento;
    }

    public int getPosicionRankingJugador1() {
        return posicionRankingJugador1;
    }


    public int getPosicionRankingJugador2() {
        return posicionRankingJugador2;
    }

}
