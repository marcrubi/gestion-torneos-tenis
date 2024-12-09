package com.ISII.gestion_torneos_tenis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import com.ISII.gestion_torneos_tenis.model.Jugador;
import com.ISII.gestion_torneos_tenis.service.RankingService;
import com.ISII.gestion_torneos_tenis.service.JugadorService;
import com.ISII.gestion_torneos_tenis.service.TorneoService;

@Controller
public class HomeController {

    private final JugadorService jugadorService;
    private final TorneoService torneoService;
    private final RankingService rankingService;

    // Constructor Injection
    public HomeController(JugadorService jugadorService, TorneoService torneoService, RankingService rankingService) {
        this.jugadorService = jugadorService;
        this.torneoService = torneoService;
        this.rankingService = rankingService;
    }

    @GetMapping("/")
    public String home() {
        return "menu"; // Retorna menu.html
    }

    @GetMapping("/main")
    public String mostrarMenuPrincipal(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"))) {

            String nombreUsuario = authentication.getName();
            Jugador jugador = jugadorService.obtenerJugadorPorNombreUsuario(nombreUsuario);
            model.addAttribute("jugador", jugador);

            // Obtener los datos
            int torneosDisponibles = torneoService.contarTorneosDisponibles();
            int posicionRanking = rankingService.obtenerPosicionRanking(jugador.getId());

            model.addAttribute("torneosDisponibles", torneosDisponibles);
            model.addAttribute("posicionRanking", posicionRanking);
        }
        return "main";
    }
}
