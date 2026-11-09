package com.appmiparqueo.appmiparqueo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "espacios")
@Getter
@Setter
@NoArgsConstructor
public class Puesto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_espacio")
    private Integer id;

    @Column(name = "id_parqueadero", nullable = false)
    private Integer parqueaderoId;

    @Column(name = "codigo_espacio", nullable = false)
    private String codigo;

    @Column(name = "tipo_espacio", nullable = false)
    private String tipo = "general";

    @Column
    private String zona;

    @Column(nullable = false)
    private String estado;

    private String nivel;
}