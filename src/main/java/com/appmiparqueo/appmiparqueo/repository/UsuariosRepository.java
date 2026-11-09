package com.appmiparqueo.appmiparqueo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.appmiparqueo.appmiparqueo.model.Usuarios;
import java.util.Optional;
import java.util.List;

public interface UsuariosRepository extends JpaRepository<Usuarios, Integer> {
    Optional<Usuarios> findByCorreo(String correo);
    Optional<Usuarios> findByCorreoAndEstado(String correo, String estado);
    List<Usuarios> findByCorreoNotOrderByNombreAsc(String correo);
    List<Usuarios> findByEstadoAndCorreoNotOrderByNombreAsc(String estado, String correo);

    default Optional<Usuarios> findByUsuario(String usuario) {
        return findByCorreo(usuario);
    }
}