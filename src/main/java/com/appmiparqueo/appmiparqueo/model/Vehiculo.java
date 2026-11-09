package com.appmiparqueo.appmiparqueo.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehiculos")
@Getter
@Setter
@NoArgsConstructor
public class Vehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehiculo")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuarios usuario;

    @Column(nullable = false, unique = true, length = 10)
    private String placa;

    @Column(name = "tipo_vehiculo", nullable = false)
    private String tipoVehiculo;

    @Column(nullable = false)
    private String marca = "No especificada";

    @Column(nullable = false)
    private String modelo = "No especificado";

    @Column(nullable = false)
    private String color = "No especificado";

    @Column(name = "codigo_qr", unique = true)
    private String codigoQr;

    @Column(nullable = false)
    private boolean activo = true;
}