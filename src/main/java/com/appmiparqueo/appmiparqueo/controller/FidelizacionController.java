package com.appmiparqueo.appmiparqueo.controller;

import com.appmiparqueo.appmiparqueo.repository.UsuariosRepository;
import com.appmiparqueo.appmiparqueo.repository.FidelizacionRepository;
import com.appmiparqueo.appmiparqueo.repository.MovimientoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FidelizacionController {

    private final UsuariosRepository usuariosRepository;
    private final FidelizacionRepository fidelizacionRepository;
    private final MovimientoRepository movimientoRepository;
    private final String adminEmail;

    public FidelizacionController(UsuariosRepository usuariosRepository, FidelizacionRepository fidelizacionRepository,
            MovimientoRepository movimientoRepository, @Value("${app.admin.email}") String adminEmail) {
        this.usuariosRepository = usuariosRepository;
        this.fidelizacionRepository = fidelizacionRepository;
        this.movimientoRepository = movimientoRepository;
        this.adminEmail = adminEmail;
    }

    @GetMapping("/fidelizacion")
    public String showFidelizacion(Model model) {
        model.addAttribute("title", "Fidelización");
        model.addAttribute("description", "Consulta los puntos y niveles registrados en el programa de fidelización.");
        var clientes = usuariosRepository.findByEstadoAndCorreoNotOrderByNombreAsc("activo", adminEmail);
        clientes.forEach(cliente -> {
            cliente.setUsuario(cliente.getCorreo());
            var fidelity = fidelizacionRepository.findByUsuario(cliente).orElse(null);
            cliente.setPuntos(fidelity == null ? 0 : fidelity.getPuntosAcumulados());
            cliente.setVisitas(Math.toIntExact(movimientoRepository.countByReserva_Usuario_CorreoAndFechaSalidaIsNotNull(cliente.getCorreo())));
        });
        model.addAttribute("clientes", clientes);
        model.addAttribute("miembrosActivos", clientes.size());
        model.addAttribute("puntosTotales", clientes.stream().mapToInt(cliente -> cliente.getPuntos() == null ? 0 : cliente.getPuntos()).sum());
        model.addAttribute("visitasTotales", clientes.stream().mapToInt(cliente -> cliente.getVisitas() == null ? 0 : cliente.getVisitas()).sum());
        return "fidelizacion";
    }
}
