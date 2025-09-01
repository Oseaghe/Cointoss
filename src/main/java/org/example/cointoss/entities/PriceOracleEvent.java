package org.example.cointoss.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "price_oracle_events")
public class PriceOracleEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cycle_id", nullable = false)
    private BettingCycle cycle;

    @Column(precision = 18, scale = 8, nullable = false)
    private BigDecimal price;

    @Column(name = "fetched_at", insertable = false, updatable = false)
    private Instant fetchedAt;

    @Column(name = "source")
    private String source;
}

