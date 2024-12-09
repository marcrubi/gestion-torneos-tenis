// src/main/java/com/ISII/gestion_torneos_tenis/controller/EmparejamientoController.java

package com.ISII.gestion_torneos_tenis.controller;

import com.ISII.gestion_torneos_tenis.model.Emparejamiento;
import com.ISII.gestion_torneos_tenis.service.EmparejamientoService;
import com.ISII.gestion_torneos_tenis.model.Torneo;
import com.ISII.gestion_torneos_tenis.repository.TorneoRepository;
import com.ISII.gestion_torneos_tenis.repository.EmparejamientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.ISII.gestion_torneos_tenis.service.TorneoService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/emparejamientos")
public class EmparejamientoController {

    @Autowired
    private EmparejamientoService emparejamientoService;

    @Autowired
    private TorneoService torneoService;

    @Autowired
    private TorneoRepository torneoRepository;

    @Autowired
    private EmparejamientoRepository emparejamientoRepository;


    /**
     * Generar emparejamientos para un torneo y ronda específica
     */

    /**
     * Calcular el ganador de un emparejamiento específico
     */
    @PostMapping("/calcular-ganador")
    public String calcularGanador(@RequestParam Long emparejamientoId,
                                  @RequestParam Long torneoId,
                                  @RequestParam int ronda,
                                  RedirectAttributes redirectAttributes) {
        ResponseEntity<String> response = emparejamientoService.calcularGanador(emparejamientoId);
        if (response.getStatusCode().is2xxSuccessful()) {
            redirectAttributes.addFlashAttribute("successMessage", response.getBody());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", response.getBody());
        }
        return "redirect:/emparejamientos/torneo/" + torneoId + "/ronda/" + ronda;
    }



    @PostMapping("/generar")
    public String generarEmparejamientos(@RequestParam("torneoId") Long torneoId,
                                         @RequestParam("ronda") int ronda,
                                         RedirectAttributes redirectAttributes) {
        ResponseEntity<String> response = emparejamientoService.generarEmparejamientos(torneoId, ronda);
        if (response.getStatusCode().is2xxSuccessful()) {
            redirectAttributes.addFlashAttribute("successMessage", response.getBody());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", response.getBody());
        }
        return "redirect:/emparejamientos/torneo/" + torneoId;
    }


}
