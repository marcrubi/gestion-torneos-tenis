# Gestión de Torneos de Tenis

## Tabla de Contenidos

1. [Descripción](#descripción)
2. [Características](#características)
3. [Tecnologías Utilizadas](#tecnologías-utilizadas)
4. [Prerrequisitos](#prerrequisitos)
5. [Instalación](#instalación)
    - [1. Clonar el Repositorio](#1-clonar-el-repositorio)
    - [2. Configurar la Base de Datos PostgreSQL](#2-configurar-la-base-de-datos-postgresql)
    - [3. Configurar las Credenciales](#3-configurar-las-credenciales)
    - [4. Construir el Proyecto](#4-construir-el-proyecto)
    - [5. Ejecutar el Proyecto](#5-ejecutar-el-proyecto)
6. [Uso](#uso)
    - [Acceso de Jugador](#acceso-de-jugador)
    - [Acceso de Administrador](#acceso-de-administrador)
7. [Estructura del Proyecto](#estructura-del-proyecto)
8. [Contribución](#contribución)
9. [Licencia](#licencia)
10. [Contacto](#contacto)
11. [Preguntas Frecuentes (FAQ)](#preguntas-frecuentes-faq)
12. [Capturas de Pantalla](#capturas-de-pantalla)
13. [Pruebas](#pruebas)
14. [Documentación Adicional](#documentación-adicional)

---

## Descripción

La aplicación **Gestión de Torneos de Tenis** es una plataforma web diseñada para administrar torneos de tenis de manera eficiente. Permite a los usuarios registrarse como jugadores, inscribirse en torneos disponibles y participar en emparejamientos automáticos. Los administradores pueden crear y gestionar torneos, generar emparejamientos y consultar rankings y estadísticas detalladas de los jugadores.

---

## Características

- **Registro y Autenticación:** Los jugadores y administradores pueden registrarse y autenticarse en la plataforma.
- **Gestión de Torneos:** Creación, edición y eliminación de torneos.
- **Inscripción de Jugadores:** Los jugadores pueden inscribirse en torneos disponibles.
- **Generación de Emparejamientos:** Emparejamientos automáticos basados en las inscripciones.
- **Interfaz Intuitiva:** Frontend construido con Thymeleaf para una experiencia de usuario fluida.

---

## Tecnologías Utilizadas

- **Backend:** Java, Spring Boot, Spring MVC, Spring Security, Spring Data JPA
- **Frontend:** Thymeleaf, HTML
- **Base de Datos:** PostgreSQL
- **Herramientas:** Maven
- **IDE Recomendado:** IntelliJ IDEA, Eclipse

---

## Prerrequisitos

Antes de comenzar, asegúrate de tener instalado lo siguiente en tu máquina:

- **Java:** JDK 17 o superior
- **Maven:** 3.6.0 o superior
- **PostgreSQL:** 13.0 o superior
- **Git:** Para clonar el repositorio
- **IDE:** IntelliJ IDEA, Eclipse, o cualquier editor de tu preferencia

### **Configuración de PostgreSQL**
1. Crea una base de datos llamada `gestion_torneos`:
   ```sql
   CREATE DATABASE gestion_torneos;
   CREATE USER torneo_user WITH PASSWORD '1234';
   GRANT ALL PRIVILEGES ON DATABASE gestion_torneos TO torneo_user;

### **Construir el Proyecto**
Ejecuta el siguiente comando para compilar el proyecto y descargar las dependencias necesarias:

   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
La aplicación estará disponible en http://localhost:8080.

---


