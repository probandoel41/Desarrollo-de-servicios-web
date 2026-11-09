package com.appmiparqueo.appmiparqueo.controller;

import com.appmiparqueo.appmiparqueo.model.Fidelizacion;
import com.appmiparqueo.appmiparqueo.model.Usuarios;
import com.appmiparqueo.appmiparqueo.repository.FidelizacionRepository;
import com.appmiparqueo.appmiparqueo.repository.MovimientoRepository;
import com.appmiparqueo.appmiparqueo.repository.UsuariosRepository;
import com.appmiparqueo.appmiparqueo.service.UsuariosAdminService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/view/usuarios")
public class UsuariosViewController {

    private final UsuariosRepository usuariosRepository;
    private final FidelizacionRepository fidelizacionRepository;
    private final MovimientoRepository movimientoRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuariosAdminService usuariosAdminService;
    private final String adminEmail;

    public UsuariosViewController(UsuariosRepository usuariosRepository, FidelizacionRepository fidelizacionRepository,
            MovimientoRepository movimientoRepository, PasswordEncoder passwordEncoder,
            UsuariosAdminService usuariosAdminService,
            @Value("${app.admin.email}") String adminEmail) {
        this.usuariosRepository = usuariosRepository;
        this.fidelizacionRepository = fidelizacionRepository;
        this.movimientoRepository = movimientoRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuariosAdminService = usuariosAdminService;
        this.adminEmail = adminEmail;
    }

    @GetMapping("/list")
    public String listUsuarios(Model model) {
        List<Usuarios> usuarios = usuariosRepository.findAll();
        usuarios.forEach(usuario -> {
            usuario.setUsuario(usuario.getCorreo());
            usuario.setRol(usuario.getCorreo().equalsIgnoreCase(adminEmail) ? 1L : 3L);
            Fidelizacion fidelizacion = fidelizacionRepository.findByUsuario(usuario).orElse(null);
            usuario.setPuntos(fidelizacion == null ? 0 : fidelizacion.getPuntosAcumulados());
            usuario.setVisitas(Math.toIntExact(movimientoRepository.countByReserva_Usuario_CorreoAndFechaSalidaIsNotNull(usuario.getCorreo())));
        });
        model.addAttribute("usuarios", usuarios);
        return "Usuarios/list";
    }

    @GetMapping("/form")
    public String formUsuario(Model model) {
        model.addAttribute("usuario", new Usuarios());
        return "Usuarios/form";
    }

    @PostMapping("/save")
    public String saveUsuario(@Valid @ModelAttribute("usuario") Usuarios usuario, BindingResult result, Model model) {
        String correo = usuario.getCorreo() == null ? "" : usuario.getCorreo().trim().toLowerCase();
        if (correo.isBlank()) {
            result.rejectValue("correo", "required", "El correo es obligatorio y se usa para iniciar sesión.");
        } else {
            usuario.setCorreo(correo);
            usuariosRepository.findByCorreo(correo).ifPresent(existing -> {
                if (!existing.getId_usuario().equals(usuario.getId_usuario())) {
                    result.rejectValue("correo", "duplicate", "Ya existe una cuenta con ese correo.");
                }
            });
        }

        String password = usuario.getContrasena();
        if (password == null || password.isBlank()) {
            if (usuario.getId_usuario() == null) {
                result.rejectValue("contrasena", "required", "La contraseña es obligatoria.");
            } else {
                Usuarios existente = usuariosRepository.findById(usuario.getId_usuario()).orElse(null);
                if (existente != null) {
                    usuario.setContrasenaHash(existente.getContrasenaHash());
                }
            }
        } else {
            usuario.setContrasenaHash(passwordEncoder.encode(password));
        }
        if (result.hasErrors()) {
            usuario.setUsuario(correo);
            return "Usuarios/form";
        }
        usuario.setEstado("activo");
        usuariosRepository.save(usuario);
        return "redirect:/view/usuarios/list";
    }

    @PostMapping("/deactivate/{id}")
    public String deactivateUsuario(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Usuarios user = usuariosRepository.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "No se encontró la cuenta.");
        } else if (user.getCorreo().equalsIgnoreCase(adminEmail)) {
            redirectAttributes.addFlashAttribute("error", "La cuenta administradora no se puede desactivar.");
        } else {
            user.setEstado("inactivo");
            usuariosRepository.save(user);
            redirectAttributes.addFlashAttribute("mensaje", "Cuenta desactivada; sus reservas e historial se conservaron.");
        }
        return "redirect:/view/usuarios/list";
    }

    @PostMapping("/delete/{id}")
    public String deleteUsuario(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            usuariosAdminService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("mensaje", "Cuenta y registros asociados eliminados permanentemente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/view/usuarios/list";
    }

    @GetMapping("/update/{id}")
    public String updateUsuario(@PathVariable Integer id, Model model) {
        Usuarios usuario = usuariosRepository.findById(id).orElse(null);
        if (usuario == null) {
            return "redirect:/view/usuarios/list";
        }
        usuario.setUsuario(usuario.getCorreo());
        model.addAttribute("usuario", usuario);
        return "Usuarios/form";
    }
}
