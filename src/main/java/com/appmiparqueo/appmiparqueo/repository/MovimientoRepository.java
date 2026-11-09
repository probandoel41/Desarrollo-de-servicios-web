package com.appmiparqueo.appmiparqueo.repository;

import com.appmiparqueo.appmiparqueo.model.Movimiento;
import com.appmiparqueo.appmiparqueo.model.Vehiculo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoRepository extends JpaRepository<Movimiento, Integer> {
    List<Movimiento> findTop100ByOrderByFechaIngresoDesc();
    List<Movimiento> findAllByOrderByFechaIngresoDesc();
    List<Movimiento> findByFechaSalidaIsNullOrderByFechaIngresoDesc();
    List<Movimiento> findByReserva_Usuario_CorreoOrderByFechaIngresoDesc(String correo);
    List<Movimiento> findByVehiculoIn(List<Vehiculo> vehiculos);
    Optional<Movimiento> findFirstByVehiculo_PlacaIgnoreCaseAndFechaSalidaIsNullOrderByFechaIngresoDesc(String placa);
    long countByFechaSalidaIsNull();
    long countByReserva_Usuario_CorreoAndFechaSalidaIsNotNull(String correo);
}