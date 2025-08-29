package org.example.cointoss.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "betting_pools", schema = "cointoss")
public class BettingPool {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "asset_pair", nullable = false, length = 50)
    private String assetPair;

    @NotNull
    @Lob
    @Column(name = "status", nullable = false)
    private String status;

    @NotNull
    @Column(name = "start_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal startPrice;

    @Column(name = "end_price", precision = 10, scale = 2)
    private BigDecimal endPrice;

    @ColumnDefault("0.00")
    @Column(name = "total_up_pool", precision = 10, scale = 2)
    private BigDecimal totalUpPool;

    @ColumnDefault("0.00")
    @Column(name = "total_down_pool", precision = 10, scale = 2)
    private BigDecimal totalDownPool;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "open_time")
    private Instant openTime;

    @Column(name = "lock_time")
    private Instant lockTime;

    @Column(name = "settlement_time")
    private Instant settlementTime;

    @OneToMany(mappedBy = "pool")
    private Set<Bet> bets = new LinkedHashSet<>();

}