package com.appmiparqueo.appmiparqueo.controller;

import com.appmiparqueo.appmiparqueo.model.Puesto;
import com.appmiparqueo.appmiparqueo.repository.PuestoRepository;
import java.util.Arrays;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OcupacionController {

    private final PuestoRepository puestoRepository;

    public OcupacionController(PuestoRepository puestoRepository) {
        this.puestoRepository = puestoRepository;
    }

    @GetMapping("/ocupacion")
    public String showOcupacion(Model model) {
        model.addAttribute("title", "Estado de Ocupación");
        model.addAttribute("description", "Consulta en tiempo real la disponibilidad de cupos del parqueadero.");
        var puestos = puestoRepository.findAllByOrderByCodigoAsc();
        model.addAttribute("puestos", puestos);
        model.addAttribute("totalPuestos", puestos.size());
        model.addAttribute("puestosDisponibles", puestoRepository.countByEstado("disponible"));
        model.addAttribute("puestosOcupados", puestoRepository.countByEstado("ocupado"));
        model.addAttribute("puestosReservados", puestoRepository.countByEstado("reservado"));
        return "ocupacion";
    }

    @PostMapping("/ocupacion/estado")
    public String updateEstado(@RequestParam Integer id, @RequestParam String estado, RedirectAttributes redirectAttributes) {
        Puesto puesto = puestoRepository.findById(id).orElse(null);
        if (puesto == null || !Arrays.asList("disponible", "reservado", "mantenimiento").contains(estado)) {
            redirectAttributes.addFlashAttribute("error", "No se pudo actualizar el puesto.");
            return "redirect:/ocupacion";
        }
        if ("ocupado".equalsIgnoreCase(puesto.getEstado())) {
            redirectAttributes.addFlashAttribute("error", "Un puesto ocupado solo se libera registrando la salida del vehículo.");
            return "redirect:/ocupacion";
        }
        puesto.setEstado(estado);
        puestoRepository.save(puesto);
        redirectAttributes.addFlashAttribute("mensaje", "Disponibilidad del puesto actualizada.");
        return "redirect:/ocupacion";
    }
}
