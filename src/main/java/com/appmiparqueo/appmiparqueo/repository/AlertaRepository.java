package com.appmiparqueo.appmiparqueo.repository;

import com.appmiparqueo.appmiparqueo.model.Alerta;
import com.appmiparqueo.appmiparqueo.model.Usuarios;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertaRepository extends JpaRepository<Alerta, Integer> {
    List<Alerta> findByResueltaOrderByFechaDesc(boolean resuelta);
    List<Alerta> findByUsuario(Usuarios usuario);
    long countByResueltaFalse();
}