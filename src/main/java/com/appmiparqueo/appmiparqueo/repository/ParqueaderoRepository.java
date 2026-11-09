package com.appmiparqueo.appmiparqueo.repository;

import com.appmiparqueo.appmiparqueo.model.Parqueadero;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParqueaderoRepository extends JpaRepository<Parqueadero, Integer> {
    Optional<Parqueadero> findFirstByEstadoOrderByIdAsc(String estado);
}