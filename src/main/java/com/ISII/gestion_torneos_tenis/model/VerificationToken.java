// src/main/java/com/ISII/gestion_torneos_tenis/model/VerificationToken.java

package com.ISII.gestion_torneos_tenis.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_tokens")
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "jugador_id", nullable = false)
    private Jugador jugador;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    // Constructores

    public VerificationToken() {}

    public VerificationToken(String token, Jugador jugador, LocalDateTime expiryDate) {
        this.token = token;
        this.jugador = jugador;
        this.expiryDate = expiryDate;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
}
