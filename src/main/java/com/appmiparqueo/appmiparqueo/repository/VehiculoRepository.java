package com.appmiparqueo.appmiparqueo.repository;

import com.appmiparqueo.appmiparqueo.model.Usuarios;
import com.appmiparqueo.appmiparqueo.model.Vehiculo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {
    Optional<Vehiculo> findByPlacaIgnoreCaseAndActivoTrue(String placa);
    List<Vehiculo> findByUsuarioAndActivoTrueOrderByPlacaAsc(Usuarios usuario);
    List<Vehiculo> findAllByUsuario(Usuarios usuario);
    @EntityGraph(attributePaths = "usuario")
    List<Vehiculo> findAllByActivoTrueOrderByPlacaAsc();
    long countByUsuarioAndActivoTrue(Usuarios usuario);
}