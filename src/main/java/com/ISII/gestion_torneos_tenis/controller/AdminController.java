// src/main/java/com/ISII/gestion_torneos_tenis/controller/AdminController.java

package com.ISII.gestion_torneos_tenis.controller;

import com.ISII.gestion_torneos_tenis.model.*;
import com.ISII.gestion_torneos_tenis.repository.*;
import com.ISII.gestion_torneos_tenis.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private TorneoRepository torneoRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private JugadorRepository jugadorRepository;

    @Autowired
    private EmparejamientoRepository emparejamientoRepository;

    @Autowired
    private ResultadoRepository resultadoRepository;

    @Autowired
    private RankingService rankingService;

    @Autowired
    private ResultadoService resultadoService;

    @Autowired
    private TorneoService torneoService;

    @Autowired
    private EmparejamientoService emparejamientoService;

    @Autowired
    private EstadisticasService estadisticasService;


    @GetMapping("/torneos")
    public String listarTorneos(Model model) {
        List<Torneo> torneos = torneoRepository.findAll();
        model.addAttribute("torneos", torneos);
        return "admin/admin_torneos";
    }

    @GetMapping("/torneos/{id}/seleccionar-jugadores")
    public String seleccionarJugadoresParaTorneo(@PathVariable("id") Long torneoId, Model model) {
        Optional<Torneo> torneoOpt = torneoRepository.findById(torneoId);
        if (torneoOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Torneo no encontrado.");
            return "error/error";
        }

        Torneo torneo = torneoOpt.get();
        List<Inscripcion> inscripcionesInscritas = inscripcionRepository.findByTorneo(torneo).stream()
                .filter(ins -> "Inscrito".equalsIgnoreCase(ins.getEstadoInscripcion()))
                .collect(Collectors.toList());

        // Obtener el ranking global ordenado
        Map<Long, Integer> rankingJugadores = rankingService.obtenerRankingGlobal();
        Map<Long, Integer> setsGanadosMap = new HashMap<>();
        Map<Long, Integer> juegosGanadosMap = new HashMap<>();
        Map<Long, Integer> juegosPerdidosMap = new HashMap<>();

        // Ordenar las inscripciones por puntos del jugador descendente (mayor a menor)
        inscripcionesInscritas.sort((ins1, ins2) -> {
            Integer puntos1 = rankingJugadores.getOrDefault(ins1.getJugador().getId(), 0);
            Integer puntos2 = rankingJugadores.getOrDefault(ins2.getJugador().getId(), 0);
            return puntos2.compareTo(puntos1); // Orden descendente
        });

        for (Inscripcion inscripcion : inscripcionesInscritas) {
            Long jugadorId = inscripcion.getJugador().getId();
            EstadisticasService.EstadisticasJugador estadisticas = estadisticasService.calcularEstadisticas(jugadorId);
            setsGanadosMap.put(jugadorId, estadisticas.getSetsGanados());
            juegosGanadosMap.put(jugadorId, estadisticas.getJuegosGanados());
            juegosPerdidosMap.put(jugadorId, estadisticas.getJuegosPerdidos());
        }

        model.addAttribute("torneo", torneo);
        model.addAttribute("jugadoresInscritos", inscripcionesInscritas);
        model.addAttribute("rankingJugadores", rankingJugadores);
        model.addAttribute("setsGanadosMap", setsGanadosMap);
        model.addAttribute("juegosGanadosMap", juegosGanadosMap);
        model.addAttribute("juegosPerdidosMap", juegosPerdidosMap);


        return "admin/admin_seleccionar_jugadores";
    }


    @PostMapping("/torneos/{id}/seleccionar-jugadores")
    public String confirmarSeleccionJugadores(@PathVariable("id") Long torneoId,
                                              @RequestParam(required = false) List<Long> jugadoresSeleccionadosIds,
                                              RedirectAttributes redirectAttributes) {
        Optional<Torneo> torneoOpt = torneoRepository.findById(torneoId);
        if (torneoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Torneo no encontrado.");
            return "redirect:/admin/torneos";
        }

        Torneo torneo = torneoOpt.get();
        List<Inscripcion> inscripcionesInscritas = inscripcionRepository.findByTorneo(torneo).stream()
                .filter(ins -> "Inscrito".equalsIgnoreCase(ins.getEstadoInscripcion()))
                .collect(Collectors.toList());

        if (jugadoresSeleccionadosIds != null && jugadoresSeleccionadosIds.size() > 16) {
            redirectAttributes.addFlashAttribute("errorMessage", "No puedes seleccionar más de 16 jugadores.");
            return "redirect:/admin/torneos/" + torneoId + "/seleccionar-jugadores";
        }

        if (jugadoresSeleccionadosIds == null || jugadoresSeleccionadosIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debes seleccionar al menos un jugador.");
            return "redirect:/admin/torneos/" + torneoId + "/seleccionar-jugadores";
        }

        for (Inscripcion inscripcion : inscripcionesInscritas) {
            if (jugadoresSeleccionadosIds.contains(inscripcion.getJugador().getId())) {
                inscripcion.setEstadoInscripcion("Confirmada");
                inscripcionRepository.save(inscripcion);
            }
        }

        torneo.setEstadoTorneo("Jugadores Seleccionados");
        torneoRepository.save(torneo);

        System.out.println("confirmarSeleccionJugadores: Generando emparejamientos de primera ronda para torneo ID " + torneoId);
        ResponseEntity<String> response = emparejamientoService.generarEmparejamientos(torneoId, 1);
        System.out.println("confirmarSeleccionJugadores: Respuesta generación primera ronda: " + response.getBody());

        if (response.getStatusCode().is2xxSuccessful()) {
            redirectAttributes.addFlashAttribute("successMessage", "Jugadores seleccionados y emparejamientos generados exitosamente.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", response.getBody());
        }

        return "redirect:/admin/torneos";
    }

    @GetMapping("/emparejamientos/sin-resultados")
    public String listarEmparejamientosSinResultados(Model model) {
        List<Torneo> todosLosTorneos = torneoRepository.findAll();
        for (Torneo t : todosLosTorneos) {
            try {
                emparejamientoService.verificarYGestionarSiguienteRonda(t.getId());
                // Después de verificar las rondas, chequear si el torneo finalizó
                torneoService.chequearYFinalizarTorneoSiCorresponde(t.getId());
            } catch (Exception e) {
                System.out.println("Error al verificar/generar rondas para el torneo ID " + t.getId() + ": " + e.getMessage());
            }
        }

        List<Emparejamiento> emparejamientosSinResultados = emparejamientoRepository.findAll().stream()
                .filter(emp -> emp.getResultados().isEmpty())
                .collect(Collectors.toList());

        System.out.println("listarEmparejamientosSinResultados: Emparejamientos sin resultados encontrados: " + emparejamientosSinResultados.size());

        model.addAttribute("emparejamientosSinResultados", emparejamientosSinResultados);
        return "admin/admin_emparejamientos_sin_resultados";
    }

    @GetMapping("/emparejamientos/{id}/ingresar-resultados")
    public String mostrarFormularioResultados(@PathVariable("id") Long emparejamientoId, Model model) {
        Optional<Emparejamiento> emparejamientoOpt = emparejamientoRepository.findById(emparejamientoId);
        if (emparejamientoOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Emparejamiento no encontrado.");
            return "error/error";
        }

        Emparejamiento emparejamiento = emparejamientoOpt.get();
        model.addAttribute("emparejamiento", emparejamiento);
        model.addAttribute("resultado", new Resultado());

        System.out.println("mostrarFormularioResultados: Mostrando formulario para emparejamiento ID " + emparejamientoId);

        return "admin/admin_ingresar_resultados";
    }


    @PostMapping("/emparejamientos/{emparejamientoId}/ingresar-resultados")
    @Transactional
    public String ingresarResultados(@PathVariable("emparejamientoId") Long emparejamientoId,
                                     @ModelAttribute Resultado resultadoForm,
                                     RedirectAttributes redirectAttributes) {

        System.out.println("ingresarResultados: Intentando ingresar resultados para emparejamiento ID " + emparejamientoId);

        Emparejamiento emparejamiento = emparejamientoRepository.findById(emparejamientoId)
                .orElseThrow(() -> new IllegalArgumentException("Emparejamiento no encontrado con ID: " + emparejamientoId));

        // Verificar que no haya resultado ya
        if (!emparejamiento.getResultados().isEmpty()) {
            System.out.println("ingresarResultados: Ya existe un resultado para el emparejamiento ID " + emparejamientoId);
            redirectAttributes.addFlashAttribute("errorMessage", "Ya existe un resultado para este emparejamiento.");
            return "redirect:/admin/emparejamientos/sin-resultados";
        }

    // Validar sets con lógica de tenis
        if (!validarSetsTenis(resultadoForm)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Los resultados de los sets no son válidos para un partido de tenis.");
            return "redirect:/admin/emparejamientos/" + emparejamientoId + "/ingresar-resultados";
        }


        // Guardar el resultado manualmente
        ResponseEntity<String> response = resultadoService.guardarResultadoManual(emparejamientoId, resultadoForm);
        if (!response.getStatusCode().is2xxSuccessful()) {
            redirectAttributes.addFlashAttribute("errorMessage", response.getBody());
            return "redirect:/admin/emparejamientos/" + emparejamientoId + "/ingresar-resultados";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Resultados ingresados exitosamente.");
        return "redirect:/admin/emparejamientos/sin-resultados";
    }


    // Método privado para validar resultados de sets de tenis
    // Regla: Un set es válido si:
    // - Un jugador tiene >=6 juegos y diferencia de 2 con el otro (6-4, 6-2, etc.)
    // - Si se llega a 6-5, se debe llegar a 7-5 o 7-6.
    // - 7-5 y 7-6 son válidos. No se aceptan >7.
    public boolean validarSet(Integer gamesJ1, Integer gamesJ2) {
        if (gamesJ1 == null || gamesJ2 == null) return false;
        int max = Math.max(gamesJ1, gamesJ2);
        int min = Math.min(gamesJ1, gamesJ2);

        if (max < 6) {
            return false;
        }

        if (max == 6 && min <= 4) {
            return true;
        }

        if (max == 7 && (min == 5 || min == 6)) {
            return true;
        }

        return false;
    }

    private boolean validarSetsTenis(Resultado resultadoForm) {
        // Valida el primer set
        if (!validarSet(resultadoForm.getSet1Jugador1Score(), resultadoForm.getSet1Jugador2Score())) {
            return false;
        }

        // Valida el segundo set
        if (!validarSet(resultadoForm.getSet2Jugador1Score(), resultadoForm.getSet2Jugador2Score())) {
            return false;
        }

        if(!validarSet(resultadoForm.getSet3Jugador1Score(), resultadoForm.getSet3Jugador2Score())){
            return false;
        }

        // Valida el cuarto set (si existe)
        Integer s4j1 = resultadoForm.getSet4Jugador1Score();
        Integer s4j2 = resultadoForm.getSet4Jugador2Score();
        if (s4j1 != null && s4j2 != null && !validarSet(s4j1, s4j2)) {
            return false;
        }

        // Valida el quinto set (si existe)
        Integer s5j1 = resultadoForm.getSet5Jugador1Score();
        Integer s5j2 = resultadoForm.getSet5Jugador2Score();
        if (s5j1 != null && s5j2 != null && !validarSet(s5j1, s5j2)) {
            return false;
        }

        // Si todos los sets son válidos
        return true;
    }


    @GetMapping("/torneos/{torneoId}/ronda/{ronda}/emparejamientos")
    public String mostrarEmparejamientos(@PathVariable("torneoId") Long torneoId,
                                         @PathVariable("ronda") int ronda,
                                         Model model) {
        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado con ID: " + torneoId));

        List<Emparejamiento> emparejamientos = emparejamientoRepository.findByTorneo_IdAndNumeroRonda(torneoId, ronda).stream()
                .filter(emp -> emp.getResultados().isEmpty())
                .collect(Collectors.toList());

        System.out.println("mostrarEmparejamientos: Mostrando emparejamientos sin resultados para torneo ID " + torneoId + ", ronda " + ronda + ". Total emparejamientos: " + emparejamientos.size());

        model.addAttribute("torneo", torneo);
        model.addAttribute("ronda", ronda);
        model.addAttribute("emparejamientos", emparejamientos);

        return "admin/admin_ingresar_resultados";
    }

    @PostMapping("/emparejamientos/{torneoId}/ronda/{ronda}/ingresar-resultados")
    public String procesarResultados(@PathVariable("torneoId") Long torneoId,
                                     @PathVariable("ronda") int ronda,
                                     @ModelAttribute ResultadosForm resultadosForm,
                                     RedirectAttributes redirectAttributes) {
        Torneo torneo = torneoRepository.findById(torneoId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado con ID: " + torneoId));

        List<ResultadosForm.ResultadoMatch> matches = resultadosForm.getMatches();

        for (ResultadosForm.ResultadoMatch match : matches) {
            Long emparejamientoId = match.getEmparejamientoId();
            Emparejamiento emparejamiento = emparejamientoRepository.findById(emparejamientoId)
                    .orElseThrow(() -> new IllegalArgumentException("Emparejamiento no encontrado con ID: " + emparejamientoId));

            if (!emparejamiento.getResultados().isEmpty()) {
                System.out.println("procesarResultados: Emparejamiento ID " + emparejamientoId + " ya tiene resultados.");
                redirectAttributes.addFlashAttribute("errorMessage", "Algunos emparejamientos ya tienen resultados.");
                return "redirect:/admin/emparejamientos/sin-resultados";
            }

            // Validar sets
            Resultado tempRes = new Resultado();
            tempRes.setSet1Jugador1Score(match.getSet1Jugador1Score());
            tempRes.setSet1Jugador2Score(match.getSet1Jugador2Score());
            tempRes.setSet2Jugador1Score(match.getSet2Jugador1Score());
            tempRes.setSet2Jugador2Score(match.getSet2Jugador2Score());
            tempRes.setSet3Jugador1Score(match.getSet3Jugador1Score());
            tempRes.setSet3Jugador2Score(match.getSet3Jugador2Score());
            tempRes.setSet4Jugador1Score(match.getSet4Jugador1Score());
            tempRes.setSet4Jugador2Score(match.getSet4Jugador2Score());
            tempRes.setSet5Jugador1Score(match.getSet5Jugador1Score());
            tempRes.setSet5Jugador2Score(match.getSet5Jugador2Score());

            if (!validarSetsTenis(tempRes)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Uno o más emparejamientos tienen sets inválidos para tenis.");
                return "redirect:/admin/emparejamientos/sin-resultados";
            }

            ResponseEntity<String> response = resultadoService.guardarResultadoManual(emparejamientoId, tempRes);
            if (!response.getStatusCode().is2xxSuccessful()) {
                redirectAttributes.addFlashAttribute("errorMessage", response.getBody());
                return "redirect:/admin/emparejamientos/sin-resultados";
            }
        }

        System.out.println("procesarResultados: Resultados ingresados para la ronda " + ronda + " del torneo ID " + torneoId);
        List<Emparejamiento> emparejamientosRondaActual = emparejamientoRepository.findByTorneo_IdAndNumeroRonda(torneoId, ronda);
        System.out.println("procesarResultados: Emparejamientos en ronda " + ronda + ": " + emparejamientosRondaActual.size());

        long countConResultados = emparejamientosRondaActual.stream().filter(emp -> !emp.getResultados().isEmpty()).count();
        System.out.println("procesarResultados: Emparejamientos con resultados: " + countConResultados + " de " + emparejamientosRondaActual.size());

        boolean todosTienenResultado = emparejamientosRondaActual.stream()
                .allMatch(emp -> emp.getResultados() != null && !emp.getResultados().isEmpty());
        System.out.println("procesarResultados: ¿Todos los emparejamientos tienen resultados? " + todosTienenResultado);

        if (todosTienenResultado) {
            int siguienteRonda = ronda + 1;
            System.out.println("procesarResultados: Todos los emparejamientos tienen resultados. Generando emparejamientos para la ronda " + siguienteRonda);
            ResponseEntity<String> response = emparejamientoService.generarEmparejamientos(torneoId, siguienteRonda);
            System.out.println("procesarResultados: Respuesta generación ronda " + siguienteRonda + ": " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                redirectAttributes.addFlashAttribute("successMessage", "Resultados ingresados y siguiente ronda generada exitosamente.");
            } else {
                String msg = response.getBody();
                if (msg != null && msg.contains("No hay suficientes jugadores")) {
                    // Finalizar torneo
                    torneoService.chequearYFinalizarTorneoSiCorresponde(torneoId);
                    redirectAttributes.addFlashAttribute("successMessage", "Resultados ingresados. El torneo ha finalizado.");
                } else {
                    redirectAttributes.addFlashAttribute("errorMessage", response.getBody());
                }
            }
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Resultados ingresados exitosamente. Aún faltan resultados de otros partidos antes de generar la siguiente ronda.");
        }

        // Por si acaso, chequear si el torneo puede finalizar aquí también
        torneoService.chequearYFinalizarTorneoSiCorresponde(torneoId);

        return "redirect:/admin/emparejamientos/sin-resultados";
    }


    @PostMapping("/torneos/{id}/seleccionar-jugadores/automatico")
    public String seleccionarJugadoresAutomaticamente(@PathVariable("id") Long torneoId, RedirectAttributes redirectAttributes) {
        Optional<Torneo> torneoOpt = torneoRepository.findById(torneoId);
        if (torneoOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Torneo no encontrado.");
            return "redirect:/admin/torneos";
        }

        Torneo torneo = torneoOpt.get();
        List<Inscripcion> inscripcionesInscritas = inscripcionRepository.findByTorneo(torneo).stream()
                .filter(ins -> "Inscrito".equalsIgnoreCase(ins.getEstadoInscripcion()))
                .collect(Collectors.toList());

        Map<Long, Integer> rankingJugadores = rankingService.obtenerRankingGlobal();

        // Ordenar inscripciones por puntos y aplicar desempate
        inscripcionesInscritas.sort((ins1, ins2) -> {
            Integer puntos1 = rankingJugadores.getOrDefault(ins1.getJugador().getId(), 0);
            Integer puntos2 = rankingJugadores.getOrDefault(ins2.getJugador().getId(), 0);
            return puntos2.compareTo(puntos1);
        });

        // Seleccionar automáticamente hasta 16 jugadores
        List<Long> jugadoresSeleccionadosIds = inscripcionesInscritas.stream()
                .limit(16)
                .map(ins -> ins.getJugador().getId())
                .collect(Collectors.toList());

        // Añadir los IDs seleccionados al modelo usando Flash Attributes
        redirectAttributes.addFlashAttribute("jugadoresSeleccionadosIds", jugadoresSeleccionadosIds);
        redirectAttributes.addFlashAttribute("successMessage", "Jugadores seleccionados automáticamente. Revise y confirme.");

        return "redirect:/admin/torneos/" + torneoId + "/seleccionar-jugadores";
    }



    public static class ResultadosForm {
        private List<ResultadoMatch> matches;

        public List<ResultadoMatch> getMatches() {
            return matches;
        }

        public void setMatches(List<ResultadoMatch> matches) {
            this.matches = matches;
        }

        public static class ResultadoMatch {
            private Long emparejamientoId;
            private Integer set1Jugador1Score;
            private Integer set1Jugador2Score;
            private Integer set2Jugador1Score;
            private Integer set2Jugador2Score;
            private Integer set3Jugador1Score;
            private Integer set3Jugador2Score;
            private Integer set4Jugador1Score; // Nuevo
            private Integer set4Jugador2Score; // Nuevo
            private Integer set5Jugador1Score; // Nuevo
            private Integer set5Jugador2Score; // Nuevo

            // Getters y Setters para los nuevos campos

            public Long getEmparejamientoId() {
                return emparejamientoId;
            }

            public void setEmparejamientoId(Long emparejamientoId) {
                this.emparejamientoId = emparejamientoId;
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
        }
    }

}
