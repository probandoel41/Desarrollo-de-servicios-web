package com.appmiparqueo.appmiparqueo.service;

import com.appmiparqueo.appmiparqueo.model.Alerta;
import com.appmiparqueo.appmiparqueo.model.Fidelizacion;
import com.appmiparqueo.appmiparqueo.model.Movimiento;
import com.appmiparqueo.appmiparqueo.model.Pago;
import com.appmiparqueo.appmiparqueo.model.Parqueadero;
import com.appmiparqueo.appmiparqueo.model.Puesto;
import com.appmiparqueo.appmiparqueo.model.Reserva;
import com.appmiparqueo.appmiparqueo.model.UbicacionVehiculo;
import com.appmiparqueo.appmiparqueo.model.Usuarios;
import com.appmiparqueo.appmiparqueo.model.Vehiculo;
import com.appmiparqueo.appmiparqueo.repository.AlertaRepository;
import com.appmiparqueo.appmiparqueo.repository.FidelizacionRepository;
import com.appmiparqueo.appmiparqueo.repository.MovimientoRepository;
import com.appmiparqueo.appmiparqueo.repository.PagoRepository;
import com.appmiparqueo.appmiparqueo.repository.ParqueaderoRepository;
import com.appmiparqueo.appmiparqueo.repository.PuestoRepository;
import com.appmiparqueo.appmiparqueo.repository.ReservaRepository;
import com.appmiparqueo.appmiparqueo.repository.UbicacionVehiculoRepository;
import com.appmiparqueo.appmiparqueo.repository.UsuariosRepository;
import com.appmiparqueo.appmiparqueo.repository.VehiculoRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParqueaderoService {
    private static final String DISPONIBLE = "disponible";
    private static final String OCUPADO = "ocupado";
    private static final BigDecimal TARIFA_HORA = new BigDecimal("4000");

    private final PuestoRepository puestoRepository;
    private final MovimientoRepository movimientoRepository;
    private final PagoRepository pagoRepository;
    private final AlertaRepository alertaRepository;
    private final UsuariosRepository usuariosRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ReservaRepository reservaRepository;
    private final FidelizacionRepository fidelizacionRepository;
    private final UbicacionVehiculoRepository ubicacionRepository;
    private final ParqueaderoRepository parqueaderoRepository;
    private final String adminEmail;

    public ParqueaderoService(PuestoRepository puestoRepository, MovimientoRepository movimientoRepository,
            PagoRepository pagoRepository, AlertaRepository alertaRepository, UsuariosRepository usuariosRepository,
            VehiculoRepository vehiculoRepository, ReservaRepository reservaRepository,
            FidelizacionRepository fidelizacionRepository, UbicacionVehiculoRepository ubicacionRepository,
            ParqueaderoRepository parqueaderoRepository, @Value("${app.admin.email}") String adminEmail) {
        this.puestoRepository = puestoRepository;
        this.movimientoRepository = movimientoRepository;
        this.pagoRepository = pagoRepository;
        this.alertaRepository = alertaRepository;
        this.usuariosRepository = usuariosRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.reservaRepository = reservaRepository;
        this.fidelizacionRepository = fidelizacionRepository;
        this.ubicacionRepository = ubicacionRepository;
        this.parqueaderoRepository = parqueaderoRepository;
        this.adminEmail = adminEmail;
    }

    @Transactional
    public Vehiculo registrarVehiculo(Usuarios cuenta, String placa, String tipo, String marca, String modelo, String color) {
        Usuarios owner = usuariosRepository.findById(cuenta.getId_usuario())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la cuenta cliente."));
        if (owner.getCorreo().equalsIgnoreCase(adminEmail)) {
            throw new IllegalArgumentException("El administrador no puede registrar vehículos de cliente.");
        }
        String normalizedPlate = normalizePlate(placa);
        if (vehiculoRepository.findByPlacaIgnoreCaseAndActivoTrue(normalizedPlate).isPresent()) {
            throw new IllegalArgumentException("La placa ya está registrada.");
        }
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setUsuario(owner);
        vehiculo.setPlaca(normalizedPlate);
        vehiculo.setTipoVehiculo(requiredValue(tipo, "tipo de vehículo"));
        vehiculo.setMarca(defaultValue(marca, "No especificada"));
        vehiculo.setModelo(defaultValue(modelo, "No especificado"));
        vehiculo.setColor(defaultValue(color, "No especificado"));
        return vehiculoRepository.save(vehiculo);
    }

    @Transactional
        public Reserva reservarEspacio(Usuarios cuenta, Integer vehiculoId, Integer puestoId,
            LocalDateTime horaInicio, LocalDateTime horaFin) {
        Usuarios owner = usuariosRepository.findById(cuenta.getId_usuario())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la cuenta cliente."));
        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el vehículo."));
        if (!vehiculo.getUsuario().getId_usuario().equals(owner.getId_usuario())) {
            throw new IllegalArgumentException("Ese vehículo no pertenece a tu cuenta.");
        }
        if (horaInicio == null || horaFin == null || !horaFin.isAfter(horaInicio)
                || Duration.between(horaInicio, horaFin).toHours() > 24) {
            throw new IllegalArgumentException("La reserva debe tener un rango válido de hasta 24 horas.");
        }
        if (movimientoRepository.findFirstByVehiculo_PlacaIgnoreCaseAndFechaSalidaIsNullOrderByFechaIngresoDesc(vehiculo.getPlaca()).isPresent()) {
            throw new IllegalArgumentException("El vehículo ya está dentro del parqueadero.");
        }
        boolean hasActiveReservation = reservaRepository.findByUsuarioAndEstadoOrderByHoraInicioDesc(owner, "reservada")
                .stream().anyMatch(reserva -> reserva.getVehiculo().getId().equals(vehiculo.getId()));
        if (hasActiveReservation) {
            throw new IllegalArgumentException("Ese vehículo ya tiene una reserva activa.");
        }

        Puesto puesto = puestoRepository.findLockedById(puestoId)
            .orElseThrow(() -> new IllegalArgumentException("El espacio seleccionado no existe."));
        if (!DISPONIBLE.equals(puesto.getEstado())) {
            throw new IllegalArgumentException("El espacio seleccionado ya no está disponible.");
        }
        puesto.setEstado("reservado");
        puestoRepository.save(puesto);

        Reserva reserva = new Reserva();
        reserva.setUsuario(owner);
        reserva.setVehiculo(vehiculo);
        reserva.setEspacio(puesto);
        reserva.setCodigo("RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
        reserva.setHoraInicio(horaInicio);
        reserva.setHoraFinEstimada(horaFin);
        reserva.setEstado("reservada");
        return reservaRepository.save(reserva);
    }

    @Transactional
    public void cancelarReserva(Usuarios cuenta, Integer reservaId) {
        Usuarios owner = usuariosRepository.findById(cuenta.getId_usuario())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la cuenta cliente."));
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la reserva."));
        if (!reserva.getUsuario().getId_usuario().equals(owner.getId_usuario())) {
            throw new IllegalArgumentException("La reserva no pertenece a tu cuenta.");
        }
        if (!"reservada".equalsIgnoreCase(reserva.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden cancelar reservas que aún no tienen ingreso.");
        }
        reserva.setEstado("cancelada");
        reservaRepository.save(reserva);
        reserva.getEspacio().setEstado(DISPONIBLE);
        puestoRepository.save(reserva.getEspacio());
    }

    @Transactional
    public Movimiento registrarIngreso(String placa, String tipoVehiculo, String correoCliente) {
        String normalizedPlate = normalizePlate(placa);
        Usuarios cliente = usuariosRepository.findByCorreoAndEstado(correoCliente, "activo")
                .orElseThrow(() -> new IllegalArgumentException("Selecciona una cuenta cliente activa."));
        if (cliente.getCorreo().equalsIgnoreCase(adminEmail)) {
            throw new IllegalArgumentException("El administrador no puede asociarse como cliente del vehículo.");
        }
        if (movimientoRepository.findFirstByVehiculo_PlacaIgnoreCaseAndFechaSalidaIsNullOrderByFechaIngresoDesc(normalizedPlate).isPresent()) {
            throw new IllegalArgumentException("Ese vehículo ya tiene un ingreso activo.");
        }

        Vehiculo vehiculo = vehiculoRepository.findByPlacaIgnoreCaseAndActivoTrue(normalizedPlate).orElse(null);
        if (vehiculo == null) {
            vehiculo = new Vehiculo();
            vehiculo.setUsuario(cliente);
            vehiculo.setPlaca(normalizedPlate);
            vehiculo.setTipoVehiculo(tipoVehiculo == null || tipoVehiculo.isBlank() ? "otro" : tipoVehiculo.trim());
            vehiculo = vehiculoRepository.save(vehiculo);
        } else if (!vehiculo.getUsuario().getId_usuario().equals(cliente.getId_usuario())) {
            throw new IllegalArgumentException("La placa ya está registrada a otro cliente.");
        }

        Reserva reserva = reservaRepository.findFirstByVehiculoAndEstadoOrderByHoraInicioDesc(vehiculo, "reservada").orElse(null);
        Puesto puesto = reserva == null ? puestoRepository.findFirstByEstadoOrderByCodigoAsc(DISPONIBLE)
            .orElseThrow(() -> new IllegalArgumentException("No hay espacios disponibles.")) : reserva.getEspacio();
        puesto.setEstado(OCUPADO);
        puestoRepository.save(puesto);

        LocalDateTime now = LocalDateTime.now();
        if (reserva == null) {
            reserva = new Reserva();
            reserva.setUsuario(cliente);
            reserva.setVehiculo(vehiculo);
            reserva.setEspacio(puesto);
            reserva.setCodigo("RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
            reserva.setHoraInicio(now);
            reserva.setHoraFinEstimada(now.plusHours(1));
        }
        reserva.setEstado("ocupada");
        reserva = reservaRepository.save(reserva);

        Movimiento movimiento = new Movimiento();
        movimiento.setVehiculo(vehiculo);
        movimiento.setReserva(reserva);
        movimiento.setFechaIngreso(now);
        movimiento.setMetodoAcceso("manual");
        movimiento.setObservaciones("Ingreso registrado desde MiParqueo");
        movimiento = movimientoRepository.save(movimiento);

        Parqueadero parqueadero = parqueaderoRepository.findFirstByEstadoOrderByIdAsc("activo").orElse(null);
        if (parqueadero != null) {
            UbicacionVehiculo ubicacion = new UbicacionVehiculo();
            ubicacion.setMovimiento(movimiento);
            ubicacion.setLatitud(parqueadero.getLatitud());
            ubicacion.setLongitud(parqueadero.getLongitud());
            ubicacion.setNivel(puesto.getNivel());
            ubicacion.setZona(puesto.getZona());
            ubicacionRepository.save(ubicacion);
        }
        return movimiento;
    }

    @Transactional
    public Pago registrarSalida(String placa, String metodo) {
        Movimiento movimiento = movimientoRepository
                .findFirstByVehiculo_PlacaIgnoreCaseAndFechaSalidaIsNullOrderByFechaIngresoDesc(normalizePlate(placa))
                .orElseThrow(() -> new IllegalArgumentException("No hay un ingreso activo para esa placa."));

        LocalDateTime now = LocalDateTime.now();
        movimiento.setFechaSalida(now);
        movimientoRepository.save(movimiento);
        Reserva reserva = movimiento.getReserva();
        reserva.setEstado("completada");
        reservaRepository.save(reserva);

        Puesto puesto = reserva.getEspacio();
        puesto.setEstado(DISPONIBLE);
        puestoRepository.save(puesto);

        long minutes = Math.max(1, Duration.between(movimiento.getFechaIngreso(), now).toMinutes());
        long hours = Math.max(1, (minutes + 59) / 60);
        Pago pago = new Pago();
        pago.setReserva(reserva);
        pago.setReferencia("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT));
        pago.setMetodo(metodo == null || metodo.isBlank() ? "Efectivo" : metodo.trim());
        pago.setValor(TARIFA_HORA.multiply(BigDecimal.valueOf(hours)));
        pago.setEstado("PENDIENTE");
        return pagoRepository.save(pago);
    }

    @Transactional
    public Pago marcarPagoComoPagado(Integer pagoId, String metodo) {
        Pago pago = pagoRepository.findById(pagoId).orElseThrow(() -> new IllegalArgumentException("No se encontró el pago."));
        if (!"pagado".equalsIgnoreCase(pago.getEstadoPago())) {
            pago.setEstado("PAGADO");
            pago.setMetodo(metodo == null || metodo.isBlank() ? pago.getMetodo() : metodo.trim());
            pago.setFecha(LocalDateTime.now());
            pagoRepository.save(pago);

            Usuarios usuario = pago.getReserva().getUsuario();
            Fidelizacion fidelizacion = fidelizacionRepository.findByUsuario(usuario).orElseGet(() -> {
                Fidelizacion nueva = new Fidelizacion();
                nueva.setUsuario(usuario);
                nueva.setPuntosAcumulados(0);
                nueva.setNivel("basico");
                return nueva;
            });
            int puntos = (fidelizacion.getPuntosAcumulados() == null ? 0 : fidelizacion.getPuntosAcumulados())
                    + pago.getValor().divideToIntegralValue(new BigDecimal("1000")).intValue();
            fidelizacion.setPuntosAcumulados(puntos);
            fidelizacion.setNivel(puntos >= 2000 ? "oro" : (puntos >= 500 ? "plata" : "basico"));
            fidelizacionRepository.save(fidelizacion);
        }
        return pago;
    }

    @Transactional
    public Alerta crearAlerta(String titulo, String mensaje, String nivel) {
        if (!Arrays.asList("INFO", "ADVERTENCIA", "CRITICA").contains(nivel)) {
            throw new IllegalArgumentException("El nivel de alerta no es válido.");
        }
        Usuarios admin = usuariosRepository.findByCorreo(adminEmail)
                .orElseThrow(() -> new IllegalStateException("No está configurada la cuenta administradora."));
        Alerta alerta = new Alerta();
        alerta.setUsuario(admin);
        alerta.setTitulo(titulo.trim());
        alerta.setMensaje(mensaje.trim());
        alerta.setNivel(nivel);
        alerta.setResuelta(false);
        return alertaRepository.save(alerta);
    }

    @Transactional
    public void resolverAlerta(Integer id) {
        Alerta alerta = alertaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("No se encontró la alerta."));
        alerta.setResuelta(true);
        alertaRepository.save(alerta);
    }

    private String normalizePlate(String placa) {
        if (placa == null || placa.isBlank()) {
            throw new IllegalArgumentException("La placa es obligatoria.");
        }
        return placa.trim().replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    private String requiredValue(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El " + field + " es obligatorio.");
        }
        return value.trim();
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}