package com.appmiparqueo.appmiparqueo.repository;

import com.appmiparqueo.appmiparqueo.model.Fidelizacion;
import com.appmiparqueo.appmiparqueo.model.Usuarios;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FidelizacionRepository extends JpaRepository<Fidelizacion, Integer> {
    Optional<Fidelizacion> findByUsuario(Usuarios usuario);
}