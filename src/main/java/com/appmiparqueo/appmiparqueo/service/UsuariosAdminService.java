package com.appmiparqueo.appmiparqueo.service;

import com.appmiparqueo.appmiparqueo.model.Movimiento;
import com.appmiparqueo.appmiparqueo.model.Pago;
import com.appmiparqueo.appmiparqueo.model.UbicacionVehiculo;
import com.appmiparqueo.appmiparqueo.model.Usuarios;
import com.appmiparqueo.appmiparqueo.repository.AlertaRepository;
import com.appmiparqueo.appmiparqueo.repository.FidelizacionRepository;
import com.appmiparqueo.appmiparqueo.repository.MovimientoRepository;
import com.appmiparqueo.appmiparqueo.repository.PagoRepository;
import com.appmiparqueo.appmiparqueo.repository.PuestoRepository;
import com.appmiparqueo.appmiparqueo.repository.ReservaRepository;
import com.appmiparqueo.appmiparqueo.repository.UbicacionVehiculoRepository;
import com.appmiparqueo.appmiparqueo.repository.UsuariosRepository;
import com.appmiparqueo.appmiparqueo.repository.VehiculoRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuariosAdminService {
    private final UsuariosRepository usuariosRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ReservaRepository reservaRepository;
    private final MovimientoRepository movimientoRepository;
    private final PagoRepository pagoRepository;
    private final UbicacionVehiculoRepository ubicacionRepository;
    private final FidelizacionRepository fidelizacionRepository;
    private final AlertaRepository alertaRepository;
    private final PuestoRepository puestoRepository;
    private final String adminEmail;

    public UsuariosAdminService(UsuariosRepository usuariosRepository, VehiculoRepository vehiculoRepository,
            ReservaRepository reservaRepository, MovimientoRepository movimientoRepository,
            PagoRepository pagoRepository, UbicacionVehiculoRepository ubicacionRepository,
            FidelizacionRepository fidelizacionRepository, AlertaRepository alertaRepository,
            PuestoRepository puestoRepository, @Value("${app.admin.email}") String adminEmail) {
        this.usuariosRepository = usuariosRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.reservaRepository = reservaRepository;
        this.movimientoRepository = movimientoRepository;
        this.pagoRepository = pagoRepository;
        this.ubicacionRepository = ubicacionRepository;
        this.fidelizacionRepository = fidelizacionRepository;
        this.alertaRepository = alertaRepository;
        this.puestoRepository = puestoRepository;
        this.adminEmail = adminEmail;
    }

    @Transactional
    public void eliminarUsuario(Integer usuarioId) {
        Usuarios usuario = usuariosRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la cuenta."));
        if (usuario.getCorreo().equalsIgnoreCase(adminEmail)) {
            throw new IllegalArgumentException("La cuenta administradora está protegida y no se puede eliminar.");
        }

        List<com.appmiparqueo.appmiparqueo.model.Vehiculo> vehiculos = vehiculoRepository.findAllByUsuario(usuario);
        List<Movimiento> movimientos = vehiculos.isEmpty()
                ? List.of()
                : movimientoRepository.findByVehiculoIn(vehiculos);
        if (movimientos.stream().anyMatch(movimiento -> movimiento.getFechaSalida() == null)) {
            throw new IllegalArgumentException("Registra la salida de sus vehículos antes de eliminar la cuenta.");
        }

        var reservas = reservaRepository.findByUsuarioOrderByHoraInicioDesc(usuario);
        List<Pago> pagos = reservas.isEmpty() ? List.of() : pagoRepository.findByReservaIn(reservas);
        List<UbicacionVehiculo> ubicaciones = movimientos.isEmpty()
                ? List.of()
                : ubicacionRepository.findByMovimientoIn(movimientos);

        ubicacionRepository.deleteAll(ubicaciones);
        pagoRepository.deleteAll(pagos);
        movimientoRepository.deleteAll(movimientos);

        reservas.forEach(reserva -> {
            var puesto = reserva.getEspacio();
            if ("reservado".equalsIgnoreCase(puesto.getEstado())) {
                puesto.setEstado("disponible");
                puestoRepository.save(puesto);
            }
        });
        reservaRepository.deleteAll(reservas);

        fidelizacionRepository.findByUsuario(usuario).ifPresent(fidelizacionRepository::delete);
        alertaRepository.deleteAll(alertaRepository.findByUsuario(usuario));
        vehiculoRepository.deleteAll(vehiculos);
        usuariosRepository.delete(usuario);
    }
}