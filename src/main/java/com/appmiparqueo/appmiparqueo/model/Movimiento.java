package com.appmiparqueo.appmiparqueo.model;

import java.time.LocalDateTime;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
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
@Table(name = "ingresos_salidas")
@Getter
@Setter
@NoArgsConstructor
public class Movimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_vehiculo", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva")
    private Reserva reserva;

    @Column(name = "hora_ingreso", nullable = false)
    private LocalDateTime fechaIngreso;

    @Column(name = "hora_salida")
    private LocalDateTime fechaSalida;

    @Column(name = "metodo_acceso", nullable = false)
    private String metodoAcceso;

    private String observaciones;

    @Transient
    private String placaTemporal;

    @Transient
    private String tipoVehiculoTemporal;

    @Transient
    private String usuarioClienteTemporal;

    @Transient
    private String puestoCodigoTemporal;

    @Transient
    private String estadoTemporal;

    public String getPlaca() {
        return vehiculo == null ? placaTemporal : vehiculo.getPlaca();
    }

    public void setPlaca(String placa) {
        this.placaTemporal = placa;
    }

    public String getTipoVehiculo() {
        return vehiculo == null ? tipoVehiculoTemporal : vehiculo.getTipoVehiculo();
    }

    public void setTipoVehiculo(String tipoVehiculo) {
        this.tipoVehiculoTemporal = tipoVehiculo;
    }

    public String getUsuarioCliente() {
        return vehiculo == null || vehiculo.getUsuario() == null ? usuarioClienteTemporal : vehiculo.getUsuario().getCorreo();
    }

    public void setUsuarioCliente(String usuarioCliente) {
        this.usuarioClienteTemporal = usuarioCliente;
    }

    public String getPuestoCodigo() {
        return reserva == null || reserva.getEspacio() == null ? puestoCodigoTemporal : reserva.getEspacio().getCodigo();
    }

    public void setPuestoCodigo(String puestoCodigo) {
        this.puestoCodigoTemporal = puestoCodigo;
    }

    public String getEstado() {
        return estadoTemporal != null ? estadoTemporal : (fechaSalida == null ? "ACTIVO" : "COMPLETADO");
    }

    public void setEstado(String estado) {
        this.estadoTemporal = estado;
    }
}