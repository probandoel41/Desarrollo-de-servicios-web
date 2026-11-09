package com.appmiparqueo.appmiparqueo.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fidelizacion")
@Getter
@Setter
@NoArgsConstructor
public class Fidelizacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fidelizacion")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuarios usuario;

    @Column(name = "puntos_acumulados", nullable = false)
    private Integer puntosAcumulados = 0;

    @Column(nullable = false)
    private String nivel = "basico";

    @Column(name = "ultima_actualizacion", insertable = false, updatable = false)
    private LocalDateTime ultimaActualizacion;
}