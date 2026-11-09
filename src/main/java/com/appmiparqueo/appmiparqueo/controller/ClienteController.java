package com.appmiparqueo.appmiparqueo.controller;

import com.appmiparqueo.appmiparqueo.model.Usuarios;
import com.appmiparqueo.appmiparqueo.repository.MovimientoRepository;
import com.appmiparqueo.appmiparqueo.repository.PagoRepository;
import com.appmiparqueo.appmiparqueo.repository.FidelizacionRepository;
import com.appmiparqueo.appmiparqueo.repository.PuestoRepository;
import com.appmiparqueo.appmiparqueo.repository.ReservaRepository;
import com.appmiparqueo.appmiparqueo.repository.VehiculoRepository;
import com.appmiparqueo.appmiparqueo.service.ParqueaderoService;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/view")
public class ClienteController {

    private final MovimientoRepository movimientoRepository;
    private final PagoRepository pagoRepository;
    private final FidelizacionRepository fidelizacionRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ReservaRepository reservaRepository;
    private final PuestoRepository puestoRepository;
    private final ParqueaderoService parqueaderoService;
    private final String adminEmail;

    public ClienteController(MovimientoRepository movimientoRepository, PagoRepository pagoRepository,
            FidelizacionRepository fidelizacionRepository, VehiculoRepository vehiculoRepository,
            ReservaRepository reservaRepository, PuestoRepository puestoRepository,
            ParqueaderoService parqueaderoService, @Value("${app.admin.email}") String adminEmail) {
        this.movimientoRepository = movimientoRepository;
        this.pagoRepository = pagoRepository;
        this.fidelizacionRepository = fidelizacionRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.reservaRepository = reservaRepository;
        this.puestoRepository = puestoRepository;
        this.parqueaderoService = parqueaderoService;
        this.adminEmail = adminEmail;
    }

    @GetMapping("/cliente")
    public String panelCliente(HttpSession session, Model model) {
        Usuarios user = (Usuarios) session.getAttribute("authenticatedUser");
        if (user == null) {
            return "redirect:/login.html";
        }
        if (user.getCorreo().equalsIgnoreCase(adminEmail)) {
            return "redirect:/view/dashboard";
        }

        var movimientos = movimientoRepository.findByReserva_Usuario_CorreoOrderByFechaIngresoDesc(user.getCorreo());
        var fidelizacion = fidelizacionRepository.findByUsuario(user).orElse(null);
        user.setPuntos(fidelizacion == null ? 0 : fidelizacion.getPuntosAcumulados());
        user.setVisitas(Math.toIntExact(movimientoRepository.countByReserva_Usuario_CorreoAndFechaSalidaIsNotNull(user.getCorreo())));
        model.addAttribute("usuario", user);
        model.addAttribute("movimientos", movimientos);
        model.addAttribute("pagos", pagoRepository.findByReserva_Usuario_CorreoOrderByIdDesc(user.getCorreo()));
        model.addAttribute("vehiculos", vehiculoRepository.findByUsuarioAndActivoTrueOrderByPlacaAsc(user));
        model.addAttribute("reservas", reservaRepository.findByUsuarioOrderByHoraInicioDesc(user));
        var puestosDisponibles = puestoRepository.findByEstadoOrderByCodigoAsc("disponible");
        model.addAttribute("puestosDisponibles", puestosDisponibles);
        model.addAttribute("espaciosDisponibles", puestosDisponibles.size());
        return "cliente";
    }

    @PostMapping("/cliente/vehiculos")
    public String agregarVehiculo(HttpSession session, @RequestParam String placa, @RequestParam String tipo,
            @RequestParam(required = false) String marca, @RequestParam(required = false) String modelo,
            @RequestParam(required = false) String color, RedirectAttributes redirectAttributes) {
        Usuarios user = (Usuarios) session.getAttribute("authenticatedUser");
        if (user == null) return "redirect:/login.html";
        try {
            parqueaderoService.registrarVehiculo(user, placa, tipo, marca, modelo, color);
            redirectAttributes.addFlashAttribute("mensaje", "Vehículo agregado a tu cuenta.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/view/cliente#vehiculos";
    }

    @PostMapping("/cliente/reservas")
        public String reservarEspacio(HttpSession session, @RequestParam Integer vehiculoId,
            @RequestParam Integer puestoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime horaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime horaFin,
            RedirectAttributes redirectAttributes) {
        Usuarios user = (Usuarios) session.getAttribute("authenticatedUser");
        if (user == null) return "redirect:/login.html";
        try {
            parqueaderoService.reservarEspacio(user, vehiculoId, puestoId, horaInicio, horaFin);
            redirectAttributes.addFlashAttribute("mensaje", "Espacio reservado correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/view/cliente#reservas";
    }

    @PostMapping("/cliente/reservas/{id}/cancelar")
    public String cancelarReserva(HttpSession session, @org.springframework.web.bind.annotation.PathVariable Integer id,
            RedirectAttributes redirectAttributes) {
        Usuarios user = (Usuarios) session.getAttribute("authenticatedUser");
        if (user == null) return "redirect:/login.html";
        try {
            parqueaderoService.cancelarReserva(user, id);
            redirectAttributes.addFlashAttribute("mensaje", "Reserva cancelada y espacio liberado.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/view/cliente#reservas";
    }
}