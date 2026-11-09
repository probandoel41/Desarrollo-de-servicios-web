package com.appmiparqueo.appmiparqueo.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notificaciones")
@Getter
@Setter
@NoArgsConstructor
public class Alerta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuarios usuario;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 255)
    private String mensaje;

    @Column(name = "tipo", nullable = false)
    private String nivel;

    @Column(name = "fecha_envio", insertable = false, updatable = false)
    private LocalDateTime fecha;

    @Column(name = "leida", nullable = false)
    private boolean resuelta;
}