package org.example.cointoss.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "betting_cycles")
public class BettingCycle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "asset_pair_id", nullable = false)
    private AssetPair assetPair;

    private Instant startTime;

    private Instant lockTime;


    private Instant endTime;

    @Enumerated(EnumType.STRING)
    private CycleStatus status;

    @Column(precision = 18, scale = 8)
    private BigDecimal startPrice;

    @Column(precision = 18, scale = 8)
    private BigDecimal endPrice;

    @OneToMany(mappedBy = "cycle")
    private Set<Bet> bets = new LinkedHashSet<>();

    @OneToMany(mappedBy = "cycle")
    private List<PriceOracleEvent> oracleEvents;

    @OneToOne(mappedBy = "cycle")
    private CommissionRecord commission;
}
