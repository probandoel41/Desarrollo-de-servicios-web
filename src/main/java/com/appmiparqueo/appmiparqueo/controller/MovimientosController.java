package com.appmiparqueo.appmiparqueo.controller;

import com.appmiparqueo.appmiparqueo.repository.MovimientoRepository;
import com.appmiparqueo.appmiparqueo.repository.PuestoRepository;
import com.appmiparqueo.appmiparqueo.repository.UsuariosRepository;
import com.appmiparqueo.appmiparqueo.service.ParqueaderoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MovimientosController {

    private final MovimientoRepository movimientoRepository;
    private final PuestoRepository puestoRepository;
    private final UsuariosRepository usuariosRepository;
    private final ParqueaderoService parqueaderoService;
        private final String adminEmail;

    public MovimientosController(MovimientoRepository movimientoRepository, PuestoRepository puestoRepository,
            UsuariosRepository usuariosRepository, ParqueaderoService parqueaderoService,
            @Value("${app.admin.email}") String adminEmail) {
        this.movimientoRepository = movimientoRepository;
        this.puestoRepository = puestoRepository;
        this.usuariosRepository = usuariosRepository;
        this.parqueaderoService = parqueaderoService;
        this.adminEmail = adminEmail;
    }

    @GetMapping("/movimientos")
    public String showMovimientos(Model model) {
        model.addAttribute("title", "Movimientos Recientes");
        model.addAttribute("description", "Consulta el historial de ingresos, salidas, reservas y actividades del parqueadero.");
        model.addAttribute("movimientos", movimientoRepository.findTop100ByOrderByFechaIngresoDesc());
        model.addAttribute("ingresosActivos", movimientoRepository.countByFechaSalidaIsNull());
        model.addAttribute("puestosDisponibles", puestoRepository.countByEstado("disponible"));
        var clientes = usuariosRepository.findByEstadoAndCorreoNotOrderByNombreAsc("activo", adminEmail);
        clientes.forEach(cliente -> cliente.setUsuario(cliente.getCorreo()));
        model.addAttribute("clientes", clientes);
        return "movimientos";
    }

    @PostMapping("/movimientos/ingreso")
        public String registrarIngreso(@RequestParam String placa, @RequestParam String tipoVehiculo,
            @RequestParam String usuarioCliente, RedirectAttributes redirectAttributes) {
        try {
            parqueaderoService.registrarIngreso(placa, tipoVehiculo, usuarioCliente);
            redirectAttributes.addFlashAttribute("mensaje", "Ingreso registrado correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/movimientos";
    }

    @PostMapping("/movimientos/salida")
    public String registrarSalida(@RequestParam String placa, @RequestParam(required = false) String metodo,
            RedirectAttributes redirectAttributes) {
        try {
            parqueaderoService.registrarSalida(placa, metodo);
            redirectAttributes.addFlashAttribute("mensaje", "Salida registrada y pago generado.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/movimientos";
    }
}
