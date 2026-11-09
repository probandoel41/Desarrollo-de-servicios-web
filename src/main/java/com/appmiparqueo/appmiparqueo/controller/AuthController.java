package com.appmiparqueo.appmiparqueo.controller;

import com.appmiparqueo.appmiparqueo.model.Usuarios;
import com.appmiparqueo.appmiparqueo.repository.UsuariosRepository;
import java.util.Locale;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UsuariosRepository usuariosRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;

    public AuthController(UsuariosRepository usuariosRepository, PasswordEncoder passwordEncoder,
            @Value("${app.admin.email}") String adminEmail) {
        this.usuariosRepository = usuariosRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody AuthRequest request) {
        String username = normalizeUsername(request.getUsuario());
        if (usuariosRepository.findByCorreo(username).isPresent()) {
            return response(HttpStatus.CONFLICT, "El usuario ya está registrado.");
        }

        Usuarios user = new Usuarios();
        user.setNombre(defaultValue(request.getNombres(), "Usuario"));
        user.setApellido(defaultValue(request.getApellidos(), "Nuevo"));
        user.setCorreo(username);
        user.setTelefono(defaultValue(request.getTelefono(), "0000000000"));
        user.setContrasenaHash(passwordEncoder.encode(request.getContrasena()));
        user.setEstado("activo");
        usuariosRepository.save(user);

        return response(HttpStatus.CREATED, "Usuario registrado correctamente.");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody AuthRequest request, HttpSession session) {
        String username = normalizeUsername(request.getUsuario());
        Usuarios user = usuariosRepository.findByCorreoAndEstado(username, "activo").orElse(null);
        if (user != null && passwordEncoder.matches(request.getContrasena(), user.getContrasenaHash())) {
            user.setUsuario(user.getCorreo());
            user.setRol(user.getCorreo().equalsIgnoreCase(adminEmail) ? 1L : 3L);
            session.setAttribute("authenticatedUser", user);
            return response(HttpStatus.OK, "Autenticación satisfactoria.");
        }
        return response(HttpStatus.UNAUTHORIZED, "Error en la autenticación.");
    }

    private static String normalizeUsername(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private static String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static ResponseEntity<Map<String, String>> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("mensaje", message));
    }

    @Getter
    @Setter
    public static class AuthRequest {
        @NotBlank
        @Email
        private String usuario;

        @NotBlank
        private String contrasena;

        private String nombres;
        private String apellidos;
        private String telefono;
    }
}