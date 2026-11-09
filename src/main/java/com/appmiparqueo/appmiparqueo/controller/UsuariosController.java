package com.appmiparqueo.appmiparqueo.controller;

import com.appmiparqueo.appmiparqueo.model.Usuarios;
import com.appmiparqueo.appmiparqueo.repository.UsuariosRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/usuarios")
public class UsuariosController {
    private final UsuariosRepository usuariosRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;

    public UsuariosController(UsuariosRepository usuariosRepository, PasswordEncoder passwordEncoder,
            @Value("${app.admin.email}") String adminEmail) {
        this.usuariosRepository = usuariosRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
    }

    @GetMapping
    public List<Usuarios> getAll() {
        List<Usuarios> users = usuariosRepository.findAll();
        users.forEach(this::setTransientFields);
        return users;
    }

    @GetMapping("/{id}")
    public Usuarios getById(@PathVariable Integer id) {
        Usuarios user = usuariosRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        setTransientFields(user);
        return user;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuarios create(@RequestBody Usuarios user) {
        if (user.getCorreo() == null || user.getCorreo().isBlank() || user.getContrasena() == null || user.getContrasena().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Correo y contraseña son obligatorios.");
        }
        if (usuariosRepository.findByCorreo(user.getCorreo().trim()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado.");
        }
        user.setCorreo(user.getCorreo().trim().toLowerCase());
        user.setContrasenaHash(passwordEncoder.encode(user.getContrasena()));
        user.setContrasena(null);
        user.setEstado("activo");
        return usuariosRepository.save(user);
    }

    @PutMapping("/{id}")
    public Usuarios update(@PathVariable Integer id, @RequestBody Usuarios changes) {
        Usuarios user = usuariosRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (changes.getNombre() != null) user.setNombre(changes.getNombre());
        if (changes.getApellido() != null) user.setApellido(changes.getApellido());
        if (changes.getTelefono() != null) user.setTelefono(changes.getTelefono());
        if (changes.getCorreo() != null && !changes.getCorreo().isBlank()) {
            String email = changes.getCorreo().trim().toLowerCase();
            usuariosRepository.findByCorreo(email).ifPresent(existing -> {
                if (!existing.getId_usuario().equals(id)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado.");
                }
            });
            user.setCorreo(email);
        }
        if (changes.getContrasena() != null && !changes.getContrasena().isBlank()) {
            user.setContrasenaHash(passwordEncoder.encode(changes.getContrasena()));
        }
        setTransientFields(user);
        return usuariosRepository.save(user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        Usuarios user = usuariosRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (user.getCorreo().equalsIgnoreCase(adminEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se puede eliminar la cuenta administradora.");
        }
        user.setEstado("inactivo");
        usuariosRepository.save(user);
    }

    private void setTransientFields(Usuarios user) {
        user.setUsuario(user.getCorreo());
        user.setRol(user.getCorreo().equalsIgnoreCase(adminEmail) ? 1L : 3L);
        user.setContrasena(null);
    }
}