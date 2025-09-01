package org.example.cointoss.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "asset_pairs")
public class AssetPair {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "base_currency_id")
    private Cryptocurrency baseCurrency;

    @ManyToOne
    @JoinColumn(name = "quote_currency_id")
    private Cryptocurrency quoteCurrency;

    @Column(name = "is_active")
    private boolean isActive = true;

    @OneToMany(mappedBy = "assetPair")
    private List<BettingCycle> bettingCycles = new LinkedList<>();
}