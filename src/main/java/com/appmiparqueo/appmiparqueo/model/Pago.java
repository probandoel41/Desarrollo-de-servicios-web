package com.appmiparqueo.appmiparqueo.model;

import java.math.BigDecimal;
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
@Table(name = "pagos")
@Getter
@Setter
@NoArgsConstructor
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reserva", nullable = false)
    private Reserva reserva;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "metodo_pago", nullable = false)
    private String metodoPago;

    @Column(name = "estado_pago", nullable = false)
    private String estadoPago = "pendiente";

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "referencia_transaccion", unique = true)
    private String referenciaTransaccion;

    @Transient
    private String placaTemporal;

    @Transient
    private String usuarioClienteTemporal;

    @Transient
    private Integer movimientoIdTemporal;

    public String getReferencia() { return referenciaTransaccion; }
    public void setReferencia(String referencia) { this.referenciaTransaccion = referencia; }
    public String getPlaca() { return reserva == null || reserva.getVehiculo() == null ? placaTemporal : reserva.getVehiculo().getPlaca(); }
    public void setPlaca(String placa) { this.placaTemporal = placa; }
    public String getMetodo() { return metodoPago; }
    public void setMetodo(String metodo) { this.metodoPago = metodo; }
    public BigDecimal getValor() { return monto; }
    public void setValor(BigDecimal valor) { this.monto = valor; }
    public String getEstado() { return estadoPago == null ? null : estadoPago.toUpperCase(java.util.Locale.ROOT); }
    public void setEstado(String estado) { this.estadoPago = estado == null ? null : estado.toLowerCase(java.util.Locale.ROOT); }
    public LocalDateTime getFecha() { return fechaPago; }
    public void setFecha(LocalDateTime fecha) { this.fechaPago = fecha; }
    public Integer getMovimientoId() { return movimientoIdTemporal; }
    public void setMovimientoId(Integer id) { this.movimientoIdTemporal = id; }
    public String getUsuarioCliente() {
        return reserva == null || reserva.getUsuario() == null ? usuarioClienteTemporal : reserva.getUsuario().getCorreo();
    }
    public void setUsuarioCliente(String usuarioCliente) { this.usuarioClienteTemporal = usuarioCliente; }
}