package com.appmiparqueo.appmiparqueo.repository;

import com.appmiparqueo.appmiparqueo.model.Pago;
import com.appmiparqueo.appmiparqueo.model.Reserva;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Integer> {
    List<Pago> findTop100ByOrderByIdDesc();
    List<Pago> findByReserva_Usuario_CorreoOrderByIdDesc(String correo);
    List<Pago> findByReservaIn(List<Reserva> reservas);
    long countByEstadoPago(String estadoPago);
}