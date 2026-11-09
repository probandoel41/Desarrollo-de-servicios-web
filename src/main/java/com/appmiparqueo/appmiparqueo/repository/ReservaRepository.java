package com.appmiparqueo.appmiparqueo.repository;

import com.appmiparqueo.appmiparqueo.model.Reserva;
import com.appmiparqueo.appmiparqueo.model.Usuarios;
import com.appmiparqueo.appmiparqueo.model.Vehiculo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaRepository extends JpaRepository<Reserva, Integer> {
    List<Reserva> findByUsuarioOrderByHoraInicioDesc(Usuarios usuario);
    List<Reserva> findByUsuarioAndEstadoOrderByHoraInicioDesc(Usuarios usuario, String estado);
    Optional<Reserva> findFirstByVehiculoAndEstadoOrderByHoraInicioDesc(Vehiculo vehiculo, String estado);
}