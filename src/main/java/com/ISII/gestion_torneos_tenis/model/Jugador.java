package com.ISII.gestion_torneos_tenis.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "jugadores")
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio.")
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios.")
    private String apellidos;

    @NotBlank(message = "El teléfono es obligatorio.")
    @Size(min = 7, max = 15, message = "El teléfono debe tener entre 7 y 15 caracteres.")
    private String telefono;

    @Email(message = "El correo electrónico debe ser válido.")
    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Column(unique = true)
    private String correoElectronico;

    @NotBlank(message = "El nombre de usuario es obligatorio.")
    @Size(min = 4, max = 20, message = "El nombre de usuario debe tener entre 4 y 20 caracteres.")
    @Column(name = "nombre_usuario", unique = true)
    private String nombreUsuario;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
    private String contrasena;

    @Column(name = "estado_verificacion", nullable = false)
    private boolean estadoVerificacion;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin = false; // Por defecto, no es administrador

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaActualizacion;

    @Column(name = "puntos", nullable = false)
    private Integer puntos = 0; // Inicializar con 0

    @Transient
    private Integer posicionRanking;



    public Jugador() {
    }

    public Jugador(String nombre, String apellidos, String telefono, String correoElectronico, String nombreUsuario, String contrasena, boolean estadoVerificacion) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.correoElectronico = correoElectronico;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.estadoVerificacion = estadoVerificacion;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public boolean isEstadoVerificacion() {
        return estadoVerificacion;
    }

    public void setEstadoVerificacion(boolean estadoVerificacion) {
        this.estadoVerificacion = estadoVerificacion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public Integer getPosicionRanking() {
        return posicionRanking;
    }

    public void setPosicionRanking(Integer posicionRanking) {
        this.posicionRanking = posicionRanking;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
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
