package com.appmiparqueo.appmiparqueo.config;

import com.appmiparqueo.appmiparqueo.model.Usuarios;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RoleAccessConfiguration implements WebMvcConfigurer {

    private final String adminEmail;

    public RoleAccessConfiguration(@Value("${app.admin.email}") String adminEmail) {
        this.adminEmail = adminEmail;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                HttpSession session = request.getSession(false);
                Usuarios user = session == null ? null : (Usuarios) session.getAttribute("authenticatedUser");
                if (user == null) {
                    response.sendRedirect("/login.html");
                    return false;
                }

                String path = request.getRequestURI();
                boolean usersManagement = path.startsWith("/view/usuarios") || path.startsWith("/api/usuarios");
                boolean isAdmin = user.getCorreo().equalsIgnoreCase(adminEmail);
                if (usersManagement && !isAdmin) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return false;
                }
                if (!isAdmin) {
                    response.sendRedirect("/view/cliente");
                    return false;
                }
                return true;
            }
        }).addPathPatterns("/dashboard.html", "/dashboard", "/view/dashboard", "/view/usuarios", "/view/usuarios/**",
            "/api/usuarios", "/api/usuarios/**", "/ocupacion", "/ocupacion/**", "/movimientos", "/movimientos/**",
            "/alertas", "/alertas/**", "/pagos", "/pagos/**", "/ubicacion-vehiculos", "/fidelizacion");
    }
}