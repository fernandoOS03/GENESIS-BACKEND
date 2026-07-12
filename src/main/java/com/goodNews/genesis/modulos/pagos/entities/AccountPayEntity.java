package com.goodNews.genesis.modulos.pagos.entities;

import com.goodNews.genesis.modulos.participantes.entities.ParticipantsEntity;
import com.goodNews.genesis.shared.enums.EstadoPagoEnum;
import com.goodNews.genesis.shared.enums.MonedasEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "cuenta_pago")
public class AccountPayEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column( name = "tarifa_congelada")
    private Double tarifaCongelada;

    @Column(name = "total_abonado")
    private Double totalAbonado;

    @Enumerated(EnumType.STRING)
    @Column(name = "moneda_congelada")
    private MonedasEnum monedaCongelada;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago")
    private EstadoPagoEnum estado;

    @OneToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "participante_id", referencedColumnName = "id")
    private ParticipantsEntity participante;

    @OneToMany(mappedBy = "accountPay",  cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransaccionEntity> transacciones = new java.util.ArrayList<>();

    @PrePersist
    public void onCreate(){
        if(this.estado == null){
            this.estado = EstadoPagoEnum.PENDIENTE;
        }
        if(this.totalAbonado == null){
            this.totalAbonado = 0.0;
        }
    }
}
