package com.goodNews.genesis.modulos.pagos.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transacciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "monto")
    private Double monto;

    @Column(name = "tipo_moneda")
    private String tipoMoneda;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "cuenta_pago_id", referencedColumnName = "id")
    private AccountPayEntity accountPay;

    @PrePersist
    public void onCreate(){
        if(this.fechaRegistro == null){
            this.fechaRegistro = LocalDateTime.now();
        }
    }
}
