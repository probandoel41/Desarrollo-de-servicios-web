package com.appmiparqueo.appmiparqueo.controller;

import com.appmiparqueo.appmiparqueo.repository.PagoRepository;
import com.appmiparqueo.appmiparqueo.service.ParqueaderoService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PagosController {

    private final PagoRepository pagoRepository;
    private final ParqueaderoService parqueaderoService;

    public PagosController(PagoRepository pagoRepository, ParqueaderoService parqueaderoService) {
        this.pagoRepository = pagoRepository;
        this.parqueaderoService = parqueaderoService;
    }

    @GetMapping("/pagos")
    public String showPagos(Model model) {
        model.addAttribute("title", "Pagos");
        model.addAttribute("description", "Gestiona los pagos, recaudos y transacciones de los vehículos.");
        var pagos = pagoRepository.findTop100ByOrderByIdDesc();
        BigDecimal recaudo = pagos.stream().filter(pago -> "pagado".equalsIgnoreCase(pago.getEstadoPago()))
            .map(pago -> pago.getMonto()).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal promedio = pagos.isEmpty() ? BigDecimal.ZERO : recaudo.divide(BigDecimal.valueOf(pagos.size()), 0, RoundingMode.HALF_UP);
        model.addAttribute("pagos", pagos);
        model.addAttribute("recaudo", recaudo);
        model.addAttribute("transacciones", pagos.size());
        model.addAttribute("pagosPendientes", pagoRepository.countByEstadoPago("pendiente"));
        model.addAttribute("ticketPromedio", promedio);
        return "pagos";
    }

    @PostMapping("/pagos/{id}/marcar")
    public String marcarPagado(@PathVariable Integer id, @RequestParam String metodo,
            RedirectAttributes redirectAttributes) {
        try {
            parqueaderoService.marcarPagoComoPagado(id, metodo);
            redirectAttributes.addFlashAttribute("mensaje", "Pago confirmado correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/pagos";
    }
}
