// src/main/java/com/ISII/gestion_torneos_tenis/controller/InscripcionController.java

package com.ISII.gestion_torneos_tenis.controller;

import com.ISII.gestion_torneos_tenis.service.InscripcionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@Controller
@RequestMapping("/inscripciones")
public class InscripcionController {

    @Autowired
    private InscripcionService inscripcionService;

    /**
     * Maneja la inscripción de un jugador en un torneo.
     */
    @PostMapping("/inscribir")
    public String inscribirTorneo(@RequestParam Long jugadorId, @RequestParam Long torneoId, Model model, Authentication authentication) {
        String resultado = inscripcionService.inscribirJugadorEnTorneo(jugadorId, torneoId);

        if (resultado.equals("Inscripción exitosa al torneo.")) {
            model.addAttribute("successMessage", resultado);
        } else {
            model.addAttribute("errorMessage", resultado);
        }

        return "torneos/torneos_disponibles";
    }

    // Puedes agregar más métodos relacionados con inscripciones si es necesario
}
