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
    private JugadorService jugadorService;

    @Autowired
    private TorneoService torneoService;

    @Autowired
    private EmparejamientoService emparejamientoService;



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

        // Ordenar las inscripciones por puntos del jugador descendente (mayor a menor)
        inscripcionesInscritas.sort((ins1, ins2) -> {
            Integer puntos1 = rankingJugadores.getOrDefault(ins1.getJugador().getId(), 0);
            Integer puntos2 = rankingJugadores.getOrDefault(ins2.getJugador().getId(), 0);
            return puntos2.compareTo(puntos1); // Orden descendente
        });

        model.addAttribute("torneo", torneo);
        model.addAttribute("jugadoresInscritos", inscripcionesInscritas);
        model.addAttribute("rankingJugadores", rankingJugadores);

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

            // Calcular sets ganados
            int setsGanadosJugador1 = 0;
            int setsGanadosJugador2 = 0;

            Integer s1j1 = resultadoForm.getSet1Jugador1Score();
            Integer s1j2 = resultadoForm.getSet1Jugador2Score();
            if (s1j1 != null && s1j2 != null) {
                if (s1j1 > s1j2) setsGanadosJugador1++; else setsGanadosJugador2++;
            }

            Integer s2j1 = resultadoForm.getSet2Jugador1Score();
            Integer s2j2 = resultadoForm.getSet2Jugador2Score();
            if (s2j1 != null && s2j2 != null) {
                if (s2j1 > s2j2) setsGanadosJugador1++; else setsGanadosJugador2++;
            }

            Integer s3j1 = resultadoForm.getSet3Jugador1Score();
            Integer s3j2 = resultadoForm.getSet3Jugador2Score();
            if (s3j1 != null && s3j2 != null) {
                if (s3j1 > s3j2) setsGanadosJugador1++; else setsGanadosJugador2++;
            }

            // Determinar ganador
            Jugador ganador;
            if (setsGanadosJugador1 > setsGanadosJugador2) {
                ganador = emparejamiento.getJugador1();
            } else if (setsGanadosJugador2 > setsGanadosJugador1) {
                ganador = emparejamiento.getJugador2();
            } else {
                System.out.println("ingresarResultados: Resultado inconsistente para emparejamiento ID " + emparejamientoId);
                redirectAttributes.addFlashAttribute("errorMessage", "El resultado de los sets es inconsistente o empate.");
                return "redirect:/admin/emparejamientos/sin-resultados";
            }

            // Calcular juegos ganados totales
            int juegosGanadosJugador1 = 0;
            int juegosGanadosJugador2 = 0;
            if (s1j1 != null && s1j2 != null) {
                juegosGanadosJugador1 += s1j1;
                juegosGanadosJugador2 += s1j2;
            }
            if (s2j1 != null && s2j2 != null) {
                juegosGanadosJugador1 += s2j1;
                juegosGanadosJugador2 += s2j2;
            }
            if (s3j1 != null && s3j2 != null) {
                juegosGanadosJugador1 += s3j1;
                juegosGanadosJugador2 += s3j2;
            }

            // Crear y guardar resultado
            Resultado resultado = new Resultado();
            resultado.setEmparejamiento(emparejamiento);
            resultado.setSet1Jugador1Score(s1j1);
            resultado.setSet1Jugador2Score(s1j2);
            resultado.setSet2Jugador1Score(s2j1);
            resultado.setSet2Jugador2Score(s2j2);
            resultado.setSet3Jugador1Score(s3j1);
            resultado.setSet3Jugador2Score(s3j2);
            resultado.setGanador(ganador);
            resultado.setJuegosGanadosJugador1(juegosGanadosJugador1);
            resultado.setJuegosGanadosJugador2(juegosGanadosJugador2);

            resultadoRepository.save(resultado);
            System.out.println("ingresarResultados: Resultado guardado para emparejamiento ID " + emparejamientoId + ", Ganador: " + ganador.getNombreUsuario());

            // **Eliminar la asignación de puntos aquí**
            // jugadorService.actualizarPuntos(ganador.getId(), emparejamiento.getTorneo().getPuntosAsignados());
            // System.out.println("ingresarResultados: Puntos actualizados para el ganador ID " + ganador.getId());

            // Verificar si todos los emparejamientos de la ronda actual tienen resultados
            int rondaActual = emparejamiento.getNumeroRonda();
            Long torneoId = emparejamiento.getTorneo().getId();
            List<Emparejamiento> emparejamientosRondaActual = emparejamientoRepository.findByTorneo_IdAndNumeroRonda(torneoId, rondaActual);

            long countConResultados = emparejamientosRondaActual.stream().filter(emp -> !emp.getResultados().isEmpty()).count();
            System.out.println("ingresarResultados: Emparejamientos con resultados: " + countConResultados + " de " + emparejamientosRondaActual.size());

            boolean todosTienenResultado = emparejamientosRondaActual.stream()
                    .allMatch(emp -> emp.getResultados() != null && !emp.getResultados().isEmpty());

            System.out.println("ingresarResultados: ¿Todos los emparejamientos tienen resultados? " + todosTienenResultado);

            if (todosTienenResultado) {
                // Todos los resultados están, intentar generar siguiente ronda
                int siguienteRonda = rondaActual + 1;
                System.out.println("ingresarResultados: Todos los emparejamientos tienen resultados. Generando emparejamientos para la ronda " + siguienteRonda);
                ResponseEntity<String> response = emparejamientoService.generarEmparejamientos(torneoId, siguienteRonda);
                System.out.println("ingresarResultados: Respuesta generación ronda " + siguienteRonda + ": " + response.getBody());

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
                // Aún faltan resultados
                redirectAttributes.addFlashAttribute("successMessage", "Resultados ingresados exitosamente. Aún faltan resultados de otros partidos antes de generar la siguiente ronda.");
            }

            // Por si acaso, chequear si el torneo puede finalizar aquí también
            torneoService.chequearYFinalizarTorneoSiCorresponde(torneoId);

            return "redirect:/admin/emparejamientos/sin-resultados";
        }



    // Método privado para validar resultados de sets de tenis
    // Regla: Un set es válido si:
    // - Un jugador tiene >=6 juegos y diferencia de 2 con el otro (6-4, 6-2, etc.)
    // - Si se llega a 6-5, se debe llegar a 7-5 o 7-6.
    // - 7-5 y 7-6 son válidos. No se aceptan >7.
    private boolean validarSet(Integer gamesJ1, Integer gamesJ2) {
        if (gamesJ1 == null || gamesJ2 == null) return true;
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
        if (!validarSet(resultadoForm.getSet1Jugador1Score(), resultadoForm.getSet1Jugador2Score())) return false;
        if (!validarSet(resultadoForm.getSet2Jugador1Score(), resultadoForm.getSet2Jugador2Score())) return false;

        Integer s3j1 = resultadoForm.getSet3Jugador1Score();
        Integer s3j2 = resultadoForm.getSet3Jugador2Score();
        if ((s3j1 != null && s3j2 != null) && (!validarSet(s3j1, s3j2))) return false;

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
        }
    }

}
