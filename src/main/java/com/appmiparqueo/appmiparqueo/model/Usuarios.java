package com.appmiparqueo.appmiparqueo.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Usuarios {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id_usuario;

    @NotBlank
    @Column(name = "nombres", nullable = false)
    private String nombre;

    @NotBlank
    @Column(name = "apellidos", nullable = false)
    private String apellido;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false, length = 15)
    private String telefono;

    @Column(name = "contrasena_hash", nullable = false)
    @JsonIgnore
    private String contrasenaHash;

    @Column(name = "fecha_registro", insertable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(nullable = false)
    private String estado = "activo";

    @Transient
    private String usuario;

    @Transient
    private Long rol;

    @Transient
    private String contrasena;

    @Transient
    private Integer puntos = 0;

    @Transient
    private Integer visitas = 0;
}
