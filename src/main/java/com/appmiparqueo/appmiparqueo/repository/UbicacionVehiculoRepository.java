package com.appmiparqueo.appmiparqueo.repository;

import com.appmiparqueo.appmiparqueo.model.UbicacionVehiculo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UbicacionVehiculoRepository extends JpaRepository<UbicacionVehiculo, Integer> {
    List<UbicacionVehiculo> findByMovimientoOrderByFechaRegistroDesc(com.appmiparqueo.appmiparqueo.model.Movimiento movimiento);
    List<UbicacionVehiculo> findByMovimientoIn(List<com.appmiparqueo.appmiparqueo.model.Movimiento> movimientos);
}