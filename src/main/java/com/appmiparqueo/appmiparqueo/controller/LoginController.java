package com.appmiparqueo.appmiparqueo.controller;

import com.appmiparqueo.appmiparqueo.model.Usuarios;
import com.appmiparqueo.appmiparqueo.repository.UsuariosRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/view")
public class LoginController {

    private final UsuariosRepository usuariosRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;

    public LoginController(UsuariosRepository usuariosRepository, PasswordEncoder passwordEncoder,
            @Value("${app.admin.email}") String adminEmail) {
        this.usuariosRepository = usuariosRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "redirect:/login.html";
    }

    @GetMapping("/dashboard")
    public String dashboardPage(HttpSession session) {
        if (session.getAttribute("authenticatedUser") == null) {
            return "redirect:/login.html";
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username, @RequestParam String password, HttpSession session) {
        Usuarios user = usuariosRepository.findByCorreoAndEstado(username.trim(), "activo").orElse(null);
        if (user != null && passwordEncoder.matches(password, user.getContrasenaHash())) {
            user.setUsuario(user.getCorreo());
            user.setRol(user.getCorreo().equalsIgnoreCase(adminEmail) ? 1L : 3L);
            session.setAttribute("authenticatedUser", user);
            if (!Long.valueOf(1L).equals(user.getRol())) {
                return "redirect:/view/cliente";
            }
            return "redirect:/view/dashboard";
        }
        return "redirect:/login.html?error";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login.html";
    }
}
