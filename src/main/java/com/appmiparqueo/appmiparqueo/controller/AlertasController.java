package com.appmiparqueo.appmiparqueo.controller;

import com.appmiparqueo.appmiparqueo.repository.AlertaRepository;
import com.appmiparqueo.appmiparqueo.service.ParqueaderoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AlertasController {

    private final AlertaRepository alertaRepository;
    private final ParqueaderoService parqueaderoService;

    public AlertasController(AlertaRepository alertaRepository, ParqueaderoService parqueaderoService) {
        this.alertaRepository = alertaRepository;
        this.parqueaderoService = parqueaderoService;
    }

    @GetMapping("/alertas")
    public String showAlertas(Model model) {
        model.addAttribute("title", "Alertas");
        model.addAttribute("description", "Revisa las novedades y situaciones que requieren atención.");
        model.addAttribute("alertas", alertaRepository.findByResueltaOrderByFechaDesc(false));
        model.addAttribute("alertasResueltas", alertaRepository.findByResueltaOrderByFechaDesc(true));
        model.addAttribute("alertasPendientes", alertaRepository.countByResueltaFalse());
        return "alertas";
    }

    @PostMapping("/alertas/nueva")
    public String crearAlerta(@RequestParam String titulo, @RequestParam String mensaje,
            @RequestParam String nivel, RedirectAttributes redirectAttributes) {
        if (titulo.isBlank() || mensaje.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Completa el título y el detalle de la alerta.");
            return "redirect:/alertas";
        }
        try {
            parqueaderoService.crearAlerta(titulo, mensaje, nivel);
            redirectAttributes.addFlashAttribute("mensaje", "Alerta registrada correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/alertas";
    }

    @PostMapping("/alertas/{id}/resolver")
    public String resolverAlerta(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            parqueaderoService.resolverAlerta(id);
            redirectAttributes.addFlashAttribute("mensaje", "Alerta resuelta.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/alertas";
    }
}
