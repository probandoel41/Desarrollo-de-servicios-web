package com.appmiparqueo.appmiparqueo.controller;

import com.appmiparqueo.appmiparqueo.repository.MovimientoRepository;
import com.appmiparqueo.appmiparqueo.repository.VehiculoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UbicacionVehiculosController {

    private final MovimientoRepository movimientoRepository;
    private final VehiculoRepository vehiculoRepository;

    public UbicacionVehiculosController(MovimientoRepository movimientoRepository, VehiculoRepository vehiculoRepository) {
        this.movimientoRepository = movimientoRepository;
        this.vehiculoRepository = vehiculoRepository;
    }

    @GetMapping("/ubicacion-vehiculos")
    public String showUbicacionVehiculos(@RequestParam(required = false) String placa, Model model) {
        model.addAttribute("title", "Ubicación de Vehículos");
        model.addAttribute("description", "Encuentra rápidamente la ubicación registrada de un vehículo.");
        model.addAttribute("vehiculosRegistrados", vehiculoRepository.findAllByActivoTrueOrderByPlacaAsc());
        model.addAttribute("placaBuscada", placa == null ? "" : placa);
        model.addAttribute("busquedaRealizada", false);
        if (placa != null && !placa.isBlank()) {
            model.addAttribute("vehiculo", movimientoRepository
                    .findFirstByVehiculo_PlacaIgnoreCaseAndFechaSalidaIsNullOrderByFechaIngresoDesc(placa.replaceAll("\\s+", ""))
                    .orElse(null));
            model.addAttribute("busquedaRealizada", true);
        }
        return "ubicacion-vehiculos";
    }
}
