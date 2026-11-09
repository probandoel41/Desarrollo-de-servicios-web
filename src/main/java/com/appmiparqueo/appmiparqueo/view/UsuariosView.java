package com.appmiparqueo.appmiparqueo.view;

import com.appmiparqueo.appmiparqueo.model.Usuarios;
import com.appmiparqueo.appmiparqueo.repository.UsuariosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UsuariosView {

    @Autowired
    private UsuariosRepository usuariosRepository;

    @GetMapping("/view/usuarios")
    public String lista(Model modelo) {
        modelo.addAttribute("usuarios", usuariosRepository.findAll());
        return "usuarios/list";
    }

    @GetMapping("/view/usuarios/update-view/{id}")
    public String update(@PathVariable Integer id, Model model) {
        Usuarios usuarios = usuariosRepository.findById(id).orElse(null);
        model.addAttribute("usuarios", usuarios);
        return "usuarios/form";
    }
}
