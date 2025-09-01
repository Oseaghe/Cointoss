package org.example.cointoss.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "bets")
public class Bet {
    @Id @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "cycle_id", nullable = false)
    private BettingCycle cycle;

    @Enumerated(EnumType.STRING)
    private BetDirection direction;

    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private BetStatus status;

    @Column(precision = 18, scale = 8)
    private BigDecimal payout;

    private Instant createdAt;
}
