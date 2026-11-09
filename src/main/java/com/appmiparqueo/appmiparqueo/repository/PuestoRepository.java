package com.appmiparqueo.appmiparqueo.repository;

import com.appmiparqueo.appmiparqueo.model.Puesto;
import java.util.List;
import java.util.Optional;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PuestoRepository extends JpaRepository<Puesto, Integer> {
    List<Puesto> findAllByOrderByCodigoAsc();
    List<Puesto> findByEstadoOrderByCodigoAsc(String estado);
    Optional<Puesto> findFirstByEstadoOrderByCodigoAsc(String estado);
    Optional<Puesto> findByCodigo(String codigo);
    long countByEstado(String estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select puesto from Puesto puesto where puesto.id = :id")
    Optional<Puesto> findLockedById(@Param("id") Integer id);
}