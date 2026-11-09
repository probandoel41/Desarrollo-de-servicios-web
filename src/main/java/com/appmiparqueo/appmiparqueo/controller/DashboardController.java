package com.appmiparqueo.appmiparqueo.controller;

import com.appmiparqueo.appmiparqueo.repository.AlertaRepository;
import com.appmiparqueo.appmiparqueo.repository.MovimientoRepository;
import com.appmiparqueo.appmiparqueo.repository.PagoRepository;
import com.appmiparqueo.appmiparqueo.repository.PuestoRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final PuestoRepository puestoRepository;
    private final MovimientoRepository movimientoRepository;
    private final PagoRepository pagoRepository;
    private final AlertaRepository alertaRepository;

    public DashboardController(PuestoRepository puestoRepository, MovimientoRepository movimientoRepository,
            PagoRepository pagoRepository, AlertaRepository alertaRepository) {
        this.puestoRepository = puestoRepository;
        this.movimientoRepository = movimientoRepository;
        this.pagoRepository = pagoRepository;
        this.alertaRepository = alertaRepository;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("title", "Dashboard");
        model.addAttribute("description", "Resumen general de la operación del parqueadero.");
        model.addAttribute("puestosDisponibles", puestoRepository.countByEstado("disponible"));
        model.addAttribute("puestosOcupados", puestoRepository.countByEstado("ocupado"));
        model.addAttribute("ingresosActivos", movimientoRepository.countByFechaSalidaIsNull());
        model.addAttribute("alertasPendientes", alertaRepository.countByResueltaFalse());
        model.addAttribute("recaudo", pagoRepository.findTop100ByOrderByIdDesc().stream()
            .filter(pago -> "pagado".equalsIgnoreCase(pago.getEstadoPago())).map(pago -> pago.getMonto())
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("movimientos", movimientoRepository.findTop100ByOrderByFechaIngresoDesc().stream().limit(10).toList());
        return "dashboard";
    }
}
