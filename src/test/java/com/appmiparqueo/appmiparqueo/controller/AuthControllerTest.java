package com.appmiparqueo.appmiparqueo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appmiparqueo.appmiparqueo.model.Usuarios;
import com.appmiparqueo.appmiparqueo.repository.UsuariosRepository;
import java.util.Optional;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthControllerTest {
    private UsuariosRepository usuariosRepository;
    private PasswordEncoder passwordEncoder;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        usuariosRepository = org.mockito.Mockito.mock(UsuariosRepository.class);
        passwordEncoder = org.mockito.Mockito.mock(PasswordEncoder.class);
        controller = new AuthController(usuariosRepository, passwordEncoder, "admin@example.com");
    }

    @Test
    void registerStoresEncodedPassword() {
        AuthController.AuthRequest request = request("New@Example.com", "plain-password");
        when(usuariosRepository.findByCorreo("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");

        ResponseEntity<?> response = controller.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(usuariosRepository).save(any(Usuarios.class));
        verify(passwordEncoder).encode("plain-password");
    }

    @Test
    void loginReturnsSuccessForMatchingPassword() {
        AuthController.AuthRequest request = request("user@example.com", "secret");
        HttpSession session = org.mockito.Mockito.mock(HttpSession.class);
        Usuarios user = new Usuarios();
        user.setCorreo("user@example.com");
        user.setContrasenaHash("encoded-password");
        when(usuariosRepository.findByCorreoAndEstado("user@example.com", "activo"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encoded-password")).thenReturn(true);

        ResponseEntity<Map<String, String>> response = controller.login(request, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Autenticación satisfactoria.", response.getBody().get("mensaje"));
        verify(session).setAttribute("authenticatedUser", user);
    }

    @Test
    void loginReturnsUnauthorizedForInvalidPassword() {
        AuthController.AuthRequest request = request("user@example.com", "wrong");
        HttpSession session = org.mockito.Mockito.mock(HttpSession.class);
        Usuarios user = new Usuarios();
        user.setCorreo("user@example.com");
        user.setContrasenaHash("encoded-password");
        when(usuariosRepository.findByCorreoAndEstado("user@example.com", "activo"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded-password")).thenReturn(false);

        ResponseEntity<Map<String, String>> response = controller.login(request, session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Error en la autenticación.", response.getBody().get("mensaje"));
    }

    private static AuthController.AuthRequest request(String username, String password) {
        AuthController.AuthRequest request = new AuthController.AuthRequest();
        request.setUsuario(username);
        request.setContrasena(password);
        return request;
    }
}